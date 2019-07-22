



		public static void main(String[] args) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("date", new Date());
			map.put("dates", Arrays.asList(new Date()));
			JsonConfig config = new JsonConfig();
			config.registerJsonValueProcessor(Date.class, new JsonValueProcessor() {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	            @Override
	            /**
	             * 处理单个Date对象
	             */
	            public Object processObjectValue(String propertyName, Object date, JsonConfig config) {
	                return simpleDateFormat.format(date);
	            }
	
	            @Override
	            /**
	             * 处理JSONArray中的Date对象
	             */
	            public Object processArrayValue(Object date, JsonConfig config) {
	                return simpleDateFormat.format(date);
	            }
		});
		System.out.println(JSONObject.fromObject(map, config).toString());
	}