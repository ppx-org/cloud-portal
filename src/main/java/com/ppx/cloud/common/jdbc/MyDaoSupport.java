package com.ppx.cloud.common.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.StringUtils;

/**
 * insert update queryPage等  操作实体对象
 * @author dengxz
 * @date 2017年11月20日
 */
public class MyDaoSupport extends JdbcDaoSupport {
	
	@Autowired
	protected void setMyJdbcTemplate(JdbcTemplate jdbcTemplate) {
		super.setJdbcTemplate(jdbcTemplate);
	}
	
	/**
     * 分页查询对象列表
     * @param c 对象类型
     * @param page 分页bean
     * @param cSql 总数sql
     * @param qSql 查询sql
     * @param paraList 查询参数
     * @return 返回对List<对象>
     */
    protected <T> List<T> queryPage(Class<T> c, Page page, StringBuilder cSql, StringBuilder qSql, List<Object> paraList) {
    	if (paraList == null) paraList = new ArrayList<Object>();
    	int totalRows = super.getJdbcTemplate().queryForObject(cSql.toString(), Integer.class, paraList.toArray());
		page.setTotalRows(totalRows);
		if (totalRows == 0) return new ArrayList<T>();
		// order by
		if (!StringUtils.isEmpty(page.getOrderName())) {
			qSql.append(" order by ").append(page.getOrderName()).append(" ").append(page.getOrderType());
		}
		qSql.append(" limit ?, ?");
		paraList.add((page.getPageNumber() - 1) * page.getPageSize());
		paraList.add(page.getPageSize());
    	List<T> r = (List<T>)super.getJdbcTemplate().query(qSql.toString(), BeanPropertyRowMapper.newInstance(c), paraList.toArray());
    	return r;
    }
    
    protected <T> List<T> queryPage(Class<T> c, Page page, StringBuilder cSql, StringBuilder qSql) {
    	return queryPage(c, page, cSql, qSql, null);
    }

	
	/**
     * 取得查询条件对象
     * @param prev 前缀sql(一般是where或and)
     * @return
     */
    protected MyCriteria createCriteria(String prev) {
    	return new MyCriteria(prev);
    }
}
