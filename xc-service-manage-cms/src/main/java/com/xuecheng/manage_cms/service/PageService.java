package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.CustomException;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.exception.ExceptionCatch;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Mr.JK
 * @create 2020-08-07  9:01
 */
@Service
public class PageService {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsConfigRepository cmsConfigRepository;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    /**
     * 页面查询方法
     * @param page  页面，从1开始记数
     * @param size  每页记录数
     * @param queryPageRequest  查询条件
     * @return
     */
    public QueryResponseResult findList( int page,int size, QueryPageRequest queryPageRequest) {
        if (queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }


        //自定义条件匹配器
        //定义条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        CmsPage cmsPage = new CmsPage();
        //设置条件值（站点id）
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置模板id作为查询条件
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置页面别名作为查询条件
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //定义条件对象Example
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        //分页参数
        if (page <= 0){
            page = 1;
        }
        page = page - 1;
        if (size <= 0){
            size = 10;
        }

        //创建分页查询参数
        Pageable pageable = PageRequest.of(page, size);

        //分页查询数据
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);

        //整理查询到的数据
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());  //数据列表
        queryResult.setTotal(all.getTotalElements());   //数据总记录数
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    /**
     * 新增页面
     * @param cmsPage
     * @return
     */
    public CmsPageResult add(CmsPage cmsPage){
        if (cmsPage == null){
            //抛出异常，非法参数异常，指定异常信息的内容

        }
        //校验页面名称，站点id，页面webpath的唯一性
        //根据页面名称，站点id，页面webpath去查cms_page集合，如果查到说明此页面已经存在，如果查询不到再继续添加
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(
                cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 == null){
            //页面已经存在
            //抛出异常，异常内容就是页面已经存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        //调用dao新增页面
        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);


    }

    /**
     * 根据页面id查询页面
     * @param id 页面id
     * @return
     */
    public CmsPage findById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return null;
    }

    /**
     * 修改页面
     * @param id 页面id
     * @param cmsPage   修改后的页面
     * @return
     */
    public CmsPageResult edit(String id, CmsPage cmsPage) {
        //根据id从数据查询页面信息
        CmsPage updateCmsPage = this.findById(id);
        if (updateCmsPage != null){
            //设置要修改的数据
            //更新模板id
            updateCmsPage.setTemplateId(cmsPage.getTemplateId());
            //更新站点id
            updateCmsPage.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            updateCmsPage.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            updateCmsPage.setPageName(cmsPage.getPageName());
            //更新访问路径
            updateCmsPage.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            updateCmsPage.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataURL
            updateCmsPage.setDataUrl(cmsPage.getDataUrl());
            //提交修改
            cmsPageRepository.save(updateCmsPage);
            return new CmsPageResult(CommonCode.SUCCESS,updateCmsPage);
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    /**
     * 根据id删除页面
     * @param id 页面id
     * @return
     */
    public ResponseResult delete(String id){
        //查询
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 根据id查询cmsconfig
     * @param id
     * @return
     */
    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }

    /**
     * 页面静态化方法
     * @param pageId
     * @return
     */
    public String getPageHtml(String pageId){
        //获取数据模型
        Map model = getModelByPageId(pageId);
        if (model == null){
            //数据模型为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        //获取页面的模板信息
        String template = getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(template)){
            //页面信息为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        //执行静态化
        String html = generateHtml(template, model);
        return html;
    }

    /**
     * 执行静态化
     * @param templateContext 模板信息
     * @param model 模型数据
     * @return
     */
    private String generateHtml(String templateContext,Map model){
        //创建配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContext);
        //向configuration配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板
        try {
            Template template = configuration.getTemplate("template");
            //调用api进行静态化
            String context = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return context;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 获取页面的模板id
     * @return
     */
    private String getTemplateByPageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            //页面为空
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISIT);
        }
        //获取页面的模板id
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            //模板id为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //查询模板信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //从GridFS中取模板文件内容

            // 根据文件id查询文件

            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //获取流中的数据
            String s = null;
            try {
                s = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return s;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return null;

    }


    /**
     * 获取数据模型
     * @param pageId
     * @return
     */
    private Map getModelByPageId(String pageId){
        //取出页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            //页面为空
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISIT);
        }
        //取出页面的dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            //页面dataUrl为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate请求获取dataUrl获取数据
        /*
            使用restTemplate远程调用需要带携带jwt令牌，这里访问的courseview接口设置为不拦截，

            改为resttemplate携带令牌，代码如下

        String authUrl = uri + "/auth/oauth/token";

        //使用LinkedMultiValueMap储存多个header信息
        LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        //设置basic认证信息
        String basicAuth = this.getHttpBasic(clientId, clientSecret);
        headers.add("Authorization",basicAuth);

        //设置请求中的body信息
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

        //凭证信息错误时候, 指定restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或者401时也要正常响应,不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });
        //远程调用令牌
        ResponseEntity<Map> responseEntity = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);

         */
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        return forEntity.getBody();
    }

    /**
     * 页面发布
     * @param pageId
     * @return
     */
    public ResponseResult post(String pageId){
        //执行页面静态化
        String pageHtml = this.getPageHtml(pageId);
        //将页面静态化文件存储到GridFs中
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        //向MQ发消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 向mq发消息
     * @param pageId
     */
    private void sendPostPage(String pageId){

        //得到页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        //创建消息对象
        HashMap<String, String> msg = new HashMap<>();
        msg.put("pageId",pageId);
        //转成json串
        String jsonString = JSON.toJSONString(msg);
        //发送给mq
        //站点id
        String siteId = cmsPage.getSiteId();

        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,jsonString);

    }

    /**
     * 保存html到GridFS
     * @param pageId
     * @param htmlContent
     * @return
     */
    private CmsPage saveHtml(String pageId,String htmlContent){
        //先得到页面的信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        ObjectId objectId = null;

        try {
            //将htmlContent内容转成输入流
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");

            //将html文件内容保存到GridFS
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());

        } catch (IOException e) {
            e.printStackTrace();
        }

        //将html文件id更新到cmsPage中
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;


    }

    /**
     * 保存页面。有就更新，没有就添加
     * @param cmsPage
     * @return
     */
    public CmsPageResult save(CmsPage cmsPage) {
        //判断页面是否存在
        CmsPage one = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (one != null){
            //更新
            return this.edit(one.getPageId(),cmsPage);
        }
        return this.add(cmsPage);
    }

    /**
     * 一键发布页面
     * @param cmsPage
     * @return
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //将页面信息存储到cms_page集合中
        CmsPageResult cmsPageResult = this.save(cmsPage);
        if (!cmsPageResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面的id
        CmsPage cmsPageSave = cmsPageResult.getCmsPage();
        String pageId = cmsPageResult.getCmsPage().getPageId();

        //将执行页面发布
        ResponseResult post = this.post(pageId);
        if (!post.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼接页面url
        //取出站点id
        String siteId = cmsPageSave.getSiteId();
        //根据站点id查站点信息
        Optional<CmsSite> cmsSiteOptional = cmsSiteRepository.findById(siteId);
        if(!cmsSiteOptional.isPresent()){
            //获取站点异常
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        CmsSite cmsSite = cmsSiteOptional.get();
        //页面url
        String pageUrl = cmsSite.getSiteDomain() + cmsSite.getSiteWebPath()
                + cmsPageSave.getPageWebPath() + cmsPageSave.getPageName();

        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);

    }


}



















