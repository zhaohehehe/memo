# sql batch update 方式

1. 拼接SQL。按照数据库类型进行拼接，但是SQL的长度有限制。
2. Spring jdbcTemplate.batchUpdate方法。
3. 
4. 执行 truncate table table_name失败：

# 相关问题

1. 方式1由于批量insert拼接SQL有长度限制，超出限制之后可能未抛出异常，但是程序卡住。导致事务未commit，未rollback，进而导致session一直不释放（sql命令kill不掉，只能在OS中kill），所以select、delete、truncate等执行极慢。

2. 如果拼接的SQL语句比较短，方式一没问题。但是如果SQL可能比较长，使用Spring jdbcTemplate.batchUpdate来进行批量更新，而且使用preparedStatement也可以防止SQL注入。

   ```
   int[] org.springframework.jdbc.core.JdbcTemplate.batchUpdate(String sql, List<Object[]> batchArgs) throws DataAccessException
   ```

   涉及到CLOB类型：

   	jdbcTemplate.batchUpdate(preparedSql, new BatchPreparedStatementSetter() {
   			@Override
   			public void setValues(PreparedStatement ps, int i) throws SQLException {
   				Object  index = get(i);
   				ps.setString(1,index.get("id"));
   				ps.setClob(2, new StringReader(index.get("paper")), 		index.get("paper").length());
   				ps.setInt(3, 100);
   			}
   			@Override
   			public int getBatchSize() {
   				return subGraphKeyList.size();
   			}
   	});
   涉及到CLOB类型查询：

   ```
   final LobHandler lobHandler = new DefaultLobHandler();
   jdbcTemplate.query(selectSql, new Object[] { id },
   	new AbstractLobStreamingResultSetExtractor() {
   		@Override
   		protected void streamData(ResultSet rs) throws SQLException, IOException {
               try {
                   do {
                       Map<String, String> map = new HashMap<>();
                       String id = rs.getString(1);
                       String paper = lobHandler.getClobAsString(rs, 2);
                       ......
                   } while (rs.next());
               } catch (DataAccessException e) {
                   logger.warn("", e);
               }
   });
   ```

   