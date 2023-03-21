package com.kh.myproduct.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductDAOImpl implements ProductDAO{
  private final NamedParameterJdbcTemplate template;
  /**
   * 등록
   *
   * @param product 상품(상품아이디,상품명,수량,가격)
   * @return 상품아이디
   */
  @Override
  public Long save(Product product) {
    StringBuffer sb = new StringBuffer();
    sb.append("insert into product(product_id,pname,quantity,price) ");
    sb.append("values(product_product_id_seq.nextval, :pname, :quantity, :price) ");

    SqlParameterSource param = new BeanPropertySqlParameterSource(product);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    template.update(sb.toString(),param,keyHolder,new String[]{"product_id"});
//    template.update(sb.toString(),param,keyHolder,new String[]{"product_id","pname"});

    long productId = keyHolder.getKey().longValue(); //상품아이디

    //String pname = (String)keyHolder.getKeys().get("pname");
    return productId;
  }

  /**
   * 조회
   *
   * @param productId 상품아이디
   * @return 상품
   */
  @Override
  public Optional<Product> findById(Long productId) {
    StringBuffer sb = new StringBuffer();
    sb.append("select product_id, pname, quantity, price ");
    sb.append("from product ");
    sb.append("where product_id = :id ");

    try {
      Map<String, Long> param = Map.of("id", productId);
//      Product product = template.queryForObject(sb.toString(), param, BeanPropertyRowMapper.newInstance(Product.class));
//      Product product = template.queryForObject(sb.toString(), param, new RowMapperImpl());
      Product product = template.queryForObject(sb.toString(), param, productRowMapper());
//      Product product = template.queryForObject(sb.toString(), param, rowMapper);
//      Product product = template.queryForObject(sb.toString(), param, rowMapper2);
      return Optional.of(product);
    }catch(EmptyResultDataAccessException e){
      //조회결과가 없는경우
      return Optional.empty();
    }
  }

  /**
   * 수정
   *
   * @param productId 상품아이디
   * @param product   수정상품
   * @return 수정된 레코드 수
   */
  @Override
  public int update(Long productId, Product product) {
    StringBuffer sb = new StringBuffer();
    sb.append("update product ");
    sb.append("   set pname = :pname, ");
    sb.append("       quantity = :quantity, ");
    sb.append("       price = :price ");
    sb.append(" where product_id = :id ");

    SqlParameterSource param = new MapSqlParameterSource()
        .addValue("pname",product.getPname())
        .addValue("quantity",product.getQuantity())
        .addValue("price",product.getPrice())
        .addValue("id",productId);

    return template.update(sb.toString(),param);
  }

  /**
   * 삭제
   *
   * @param productId 상품아이디
   * @return 삭제된 레코드 수
   */
  @Override
  public int delete(Long productId) {
    String sql = "delete from product where product_id = :id ";
    return template.update(sql,Map.of("id",productId));
  }

  /**
   * 전체 삭제
   *
   * @return 삭제한 레코드 건수
   */
  @Override
  public int deleteAll() {
    String sql = "delete from product";
    Map<String,String> param = new LinkedHashMap<>();
    int deletedRowCnt = template.update(sql, param);
    return deletedRowCnt;
  }

  /**
   * 목록
   *
   * @return 상품목록
   */
  @Override
  public List<Product> findAll() {
    StringBuffer sb = new StringBuffer();
    sb.append("select product_id, pname, quantity, price ");
    sb.append("from product ");

    List<Product> list = template.query(
        sb.toString(),
        BeanPropertyRowMapper.newInstance(Product.class)
        //레코드 컬럼과 자바객체 멤버필드가 동일한 이름일 경우, camelcase 지원
    );

    return list;
  }

  class RowMapperImpl implements RowMapper<Product>{
    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
      Product product = new Product();
      product.setProductId(rs.getLong("product_id"));
      product.setPname(rs.getString("pname"));
      product.setQuantity(rs.getLong("quantity"));
      product.setPrice(rs.getLong("price"));
      return product;
    }
  };

  //익명 클래스로 생성된 객체를 RowMapper 인터페이스로 받음
  RowMapper<Product> rowMapper = new RowMapper<Product>() {
    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
      Product product = new Product();
      product.setProductId(rs.getLong("product_id"));
      product.setPname(rs.getString("pname"));
      product.setQuantity(rs.getLong("quantity"));
      product.setPrice(rs.getLong("price"));
      return product;
    }
  };

  RowMapper<Product> rowMapper2 = (rs, rowNum) -> {
    Product product = new Product();
    product.setProductId(rs.getLong("product_id"));
    product.setPname(rs.getString("pname"));
    product.setQuantity(rs.getLong("quantity"));
    product.setPrice(rs.getLong("price"));
    return product;
  };

  //수동 매핑
  private RowMapper<Product> productRowMapper() {
    return (rs, rowNum) -> {
      Product product = new Product();
      product.setProductId(rs.getLong("product_id"));
      product.setPname(rs.getString("pname"));
      product.setQuantity(rs.getLong("quantity"));
      product.setPrice(rs.getLong("price"));
      return product;
    };
  }

  //자동 매핑 : 테이블의 컬럼명과 자바객체 타입의 멤버필드가 같아야 한다
  // new BeanPropertyRowMapper.newInstance(자바객체타입)

  /**
   * 상품존재유무
   *
   * @param productId 상품아이디
   * @return
   */
  @Override
  public boolean isExist(Long productId) {
    boolean isExist = false;
    String sql = "select count(*) from product where product_id = :product_id ";

    Map<String,Long> param = Map.of("product_id",productId);
    Integer integer = template.queryForObject(sql, param, Integer.class);
    isExist = (integer>0)? true : false;
    return isExist;
  }

  /**
   * 등록된 상품수
   *
   * @return 레코드 건수
   */
  @Override
  public int countOfRecord() {
    String sql = "select count(*) from product ";
    Map<String,String> param = new LinkedHashMap<>();
    Integer rows = template.queryForObject(sql, param, Integer.class);
    return rows;
  }
}