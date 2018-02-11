package com.wyf.demo.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DemoController {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@ResponseBody
	@GetMapping("/demo")
	public String demo() {
		Map<String, String> map = new HashMap<>();
		map.put("success", "true");
		return map.toString();
	}
	
	
	@GetMapping("/demo2")
	public String dem2o() {
		return "index.html";
	}
	
	@ResponseBody
	@GetMapping("/executeSql")
	public List<Map<String, String>> executeSql(String sql) {
		Connection con = null;
		try {
			if(sql == null) {
				sql = "select * from tpl_user_t";
			}
			if(sql.toUpperCase().trim().startsWith("SELECT ")) {
				List<Map<String, String>> list =new ArrayList<>();
				RowCallbackHandler rowMapper = new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						ResultSetMetaData md = rs.getMetaData();
						int count = md.getColumnCount();
						Map<String, String> row = new HashMap<>();
						for (int i = 1; i <= count; i++) {
							String cm = md.getColumnName(i);
							row.put(cm, rs.getString(cm));
							
						}
						list.add(row);
					}
				};
				jdbcTemplate.query(sql, new Object[0], rowMapper);
				return list;
			}else {
				jdbcTemplate.update(sql);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
		
	}
	
}
