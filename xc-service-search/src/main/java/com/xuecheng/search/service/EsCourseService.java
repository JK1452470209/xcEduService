package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.JK
 * @create 2020-08-18  22:03
 */
@Service
public class EsCourseService {

    @Value("${xuecheng.course.index}")
    private String index;

    @Value("${xuecheng.media.index}")
    private String media_index;

    @Value("${xuecheng.course.type}")
    private String type;

    @Value("${xuecheng.media.type}")
    private String media_type;

    @Value("${xuecheng.course.source_field}")
    private String sourse_fieId;

    @Value("${xuecheng.media.source_field}")
    private String media_sourse_fieId;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 课程列表搜索
     * @param page 页码
     * @param size 每页数量
     * @param courseSearchParam 搜索参数
     * @return
     */
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        if (courseSearchParam == null){
            courseSearchParam = new CourseSearchParam();
        }

        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置搜索类型
        searchRequest.types(type);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤源字段
        String[] sourse_fieId_array = sourse_fieId.split(",");
        searchSourceBuilder.fetchSource(sourse_fieId_array,new String[]{});
        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //搜索条件
        //根据关键字搜索
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan")
                    .minimumShouldMatch("70%")
                    .field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())){
            //根据一级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())){
            //根据一级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            //根据一级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        //设置分页参数
        if(page<=0){
            page = 1;
        }
        if(size<=0){
            size = 20;
        }
        //计算搜索起始位置
        int start = (page-1) * size;
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(size);

        //设置boolQueryBuilder到searchSourceBuilder
        searchSourceBuilder.query(boolQueryBuilder);

        //配置高亮信息
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        //请求搜索
        searchRequest.source(searchSourceBuilder);

        QueryResult<CoursePub> queryResult = new QueryResult<>();
        ArrayList<CoursePub> list = new ArrayList<>();
        try {
            //执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            //获取响应结果
            SearchHits hits = searchResponse.getHits();
            //匹配的总记录数
            long totalHits = hits.totalHits;
            queryResult.setTotal(totalHits);
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit:searchHits){
                CoursePub coursePub = new CoursePub();
                //原文档
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                //取出name
                String name = (String) sourceAsMap.get("name");
                coursePub.setName(name);

                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if(highlightFields.get("name")!=null){
                    HighlightField highlightField = highlightFields.get("name");
                    Text[] fragments = highlightField.fragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for(Text text:fragments){
                        stringBuffer.append(text);
                    }
                    name = stringBuffer.toString();
                }
                coursePub.setName(name);
                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //优惠后的价格
                Float price = null;
                try {
                    if(sourceAsMap.get("price") !=null){
                        price = Float.parseFloat(String.format("%.3f",sourceAsMap.get("price")));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                coursePub.setPrice(price);
                //优惠前的价格
                Float priceOld = null;
                try {
                    if(sourceAsMap.get("price_old") !=null){
                        priceOld = Float.parseFloat(String.format("%.3f",sourceAsMap.get("price_old")));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                coursePub.setPrice_old(priceOld);
                //将coursePub放进list中
                list.add(coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        queryResult.setList(list);
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);

        return queryResponseResult;
    }

    /**
     * 根据id查询课程发布信息
     * 使用ES的客户端向ES请求查询索引信息
     * @param id
     * @return
     */
    public Map<String, CoursePub> getall(String id) {
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //指定type
        searchRequest.types(type);
        //定义SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termQuery
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));

        searchRequest.source(searchSourceBuilder);
        HashMap<String, CoursePub> map = new HashMap<>();
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit:searchHits){
                //获取原文档内容
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //最终要返回的课程信息
                CoursePub coursePub = new CoursePub();
                String courseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setCharge(charge);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(courseId,coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 根据多个课程计划id查询课程媒资信息
     * @param teachplanIds
     * @return
     */
    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {
        //定义一个搜索对象
        //设置索引
        SearchRequest searchRequest = new SearchRequest(media_index);

        //设置类型
        searchRequest.types(media_type);

        //创建搜索源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //设置使用termsQuery根据多个id查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));

        //源字段过滤
        String[] media_index_arr = media_sourse_fieId.split(",");
        searchSourceBuilder.fetchSource(media_index_arr, new String[]{});
        searchRequest.source(searchSourceBuilder);

        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        SearchResponse searchResponse = null;
        long totalHits = 0;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
            //获取结果
            SearchHits hits = searchResponse.getHits();
            totalHits = hits.getTotalHits();
            SearchHit[] searchHits = hits.getHits();
            for(SearchHit hit:searchHits){
                TeachplanMediaPub teachplanMediaPub =new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出课程计划媒资信息
                String courseid = (String) sourceAsMap.get("courseid");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setTeachplanId(teachplan_id);
                //将对象加入到列表中
                teachplanMediaPubList.add(teachplanMediaPub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //构建返回课程媒资信息对象
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        queryResult.setTotal(totalHits);
        return new QueryResponseResult<TeachplanMediaPub>(CommonCode.SUCCESS,queryResult);
    }
}
