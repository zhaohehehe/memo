package zhaohe.study.dao.impl;

import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import zhaohe.study.dao.IDatabaseDistributedLockUtilDao;

@Repository("databaseDistributedLockUtilDao")
public class DatabaseDistributedLockUtilDao implements IDatabaseDistributedLockUtilDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDistributedLockUtilDao.class);
	@Autowired
	JdbcTemplate jdbcTemplate;
	@Override
	public boolean lock(int time, TimeUnit unit, String lockType, String lockKey, String lockExpired, String lockOwner,
			String description) {
		try {
			tryLockTimeout(time, unit, lockType, lockKey, lockExpired, lockOwner, description);
			return true;
		} catch (InterruptedException e) {
			LOGGER.error("", e);
			Thread.currentThread().interrupt();
			return false;

		}
	}

	@Override
	public boolean lock(String lockType, String lockKey, String lockExpired, String lockOwner, String description) {
		return tryLock(lockType, lockKey, lockExpired, lockOwner, description);
	}

	@Override
	public void tryLockTimeout(int time, TimeUnit unit, String lockType, String lockKey, String lockExpired,
			String lockOwner, String description) throws InterruptedException {
		long startLine = System.nanoTime();
		while (!tryLock(lockType, lockKey, lockExpired, lockOwner, description)) {
			if (System.currentTimeMillis() > startLine + unit.toNanos(time)) {
				StringBuilder msg = new StringBuilder().append("获取lock{").append("lockType=").append(lockType)
						.append("lockKey=").append(lockKey).append("lockExpired=").append(lockExpired)
						.append("lockOwner=").append(lockOwner).append("description=").append(description)
						.append("}超时！");
				throw new InterruptedException(msg.toString());
			} else {
				TimeUnit.SECONDS.sleep(1);
			}
		}
	}

	/**
	 * ！！！！！！！！！！！！Oracle数据库clob插入失败
	 */
	@Override
	public boolean tryLock(final String lockType, final String lockKey, final String lockExpired, String lockOwner,
			final String description) {
		Long.valueOf(lockExpired);
		final String oid = UUID.randomUUID().toString();

		if ("oracle".equalsIgnoreCase("oracle")) {
			try {
				String insertSql = "insert into distributed_lock(oid,lock_type,lock_key,lock_expired,lock_owner,lock_time) values (?, ?, ?, ?, ?, ?)";
				int affectRows = jdbcTemplate.update(insertSql, oid, lockType, lockKey, lockExpired,
						lockOwner, getLockTime());
				return affectRows > 0;
			} catch (Exception e) {
				StringBuilder msg = new StringBuilder().append("获取lock{").append("lockType=").append(lockType)
						.append("lockKey=").append(lockKey).append("lockExpired=").append(lockExpired)
						.append("lockOwner=").append(lockOwner).append("description=").append(description)
						.append("}失败！");
				LOGGER.error(msg.toString(), e);
				return false;
			}
		} else {
			try {
				String insertSql = "insert into distributed_lock(oid,lock_type,lock_key,lock_expired,lock_owner,lock_time,lock_description) values (?, ?, ?, ?, ?, ?, ?)";
				int affectRows = jdbcTemplate.update(insertSql, oid, lockType, lockKey, lockExpired,
						lockOwner, getLockTime(), description);
				return affectRows > 0;
			} catch (Exception e) {
				StringBuilder msg = new StringBuilder().append("获取lock{").append("lockType=").append(lockType)
						.append("lockKey=").append(lockKey).append("lockExpired=").append(lockExpired)
						.append("lockOwner=").append(lockOwner).append("description=").append(description)
						.append("}失败！");
				LOGGER.error(msg.toString(), e);
				return false;
			}

		}
	}

	@Override
	public boolean unlock(String lockType, String lockKey) {
		String unLockSql = "delete from distributed_lock where lock_type = ? and lock_key = ?";
		int affectRows = jdbcTemplate.update(unLockSql, lockType, lockKey);
		return affectRows > 0;

	}

	@Override
	public boolean unlock(String lockType, String lockKey, String unlockOwner) {
		String selectSql = "select lock_owner as \"lockOwner\" from distributed_lock where lock_type = ? and lock_key = ? group by lock_type,lock_key";
		String lockOwner = jdbcTemplate.queryForObject(selectSql, String.class, lockType, lockKey);
		if (lockOwner.equals(unlockOwner)) {
			return unlock(lockType, lockKey);
		} else {
			StringBuilder msg = new StringBuilder().append("删除lock{").append("lockType=").append(lockType)
					.append("lockKey=").append(lockKey).append("}失败！lockOwner与unlockOwner不一致！").append("lockOwner=")
					.append(lockOwner).append(",unlockOwner=").append(unlockOwner);
			LOGGER.error(msg.toString());
			return false;
		}

	}

	private static String getLockTime() {
		return String.valueOf(Calendar.getInstance().getTimeInMillis());
	}

}
