package com.bestpricemarket.persistence;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import com.bestpricemarket.domain.GoodsVO;

@Repository
public class GoodsDAOImpl implements GoodsDAO {
	
	// DB 의존 주입
	@Inject
	private SqlSession sqlSession;
	
	private static final String namespace = "com.bestpricemarket.mappers.goodsMapper";
	
	// 상품등록
	@Override
	public void registerGoods(GoodsVO vo) throws Exception {
		sqlSession.insert(namespace+".register",vo);
		System.out.println("DAO : 상품등록"+vo);
		
	}
	
	// 상품목록
		
	// 상품조회(상품상세페이지)
		
	// 상품수정
		
	// 상품삭제

}
