package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.spring.web.json.Json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Mr.JK
 * @create 2020-08-12  21:43
 */
@Service
public class CourseService {

    //从配置文件获取课程发布的基本配置
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;


    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    CourseMapper courseMapper;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Autowired
    CmsPageClient cmsPageClient;




    /**
     * 查询课程计划并按树结构返回
     * @param courseId
     * @return
     */
    public TeachplanNode findTeachplanList(String courseId){
        return teachplanMapper.selectList(courseId);
    }

    /**
     * 添加课程计划
     * @param teachplan
     * @return
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String courseid = teachplan.getCourseid();
        //页面传入的parentId
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){
            //取出该课程的根结点
            parentid = this.getTeachplanRoot(courseid);

        }
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        //父节点的级别
        String grade = parentNode.getGrade();
        //新节点
        Teachplan teachplanNew = new Teachplan();
        //将页面提交的teachplan信息拷贝到teachplanNew对象中
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        if (grade.equals("1")) {
            teachplanNew.setGrade("2");//级别，根据父节点的级别来设置
        }else {
            teachplanNew.setGrade("3");
        }
        teachplanRepository.save(teachplanNew);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 分页查询课程信息
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */
    @Transactional
    public QueryResponseResult findCourseList(String company_id,int page, int size, CourseListRequest courseListRequest){
        if (page <= 0){
            page = 1;
        }
        if (size <= 0){
            size = 4;
        }
        if(courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        courseListRequest.setCompanyId(company_id);


        PageHelper.startPage(1,7);
        Page<CourseInfo> courseInfos = courseMapper.findCoursePageList(courseListRequest);

        long total = courseInfos.getTotal();
        List<CourseInfo> result = courseInfos.getResult();

        QueryResult queryResult = new QueryResult();
        queryResult.setTotal(total);
        queryResult.setList(result);
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }


    /**
     * 查询课程的根结点，如果查询不到要自动添加根结点
     * @param courseId
     * @return
     */
    private String getTeachplanRoot(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()){
            return null;
        }
        //课程信息
        CourseBase courseBase = optional.get();
        //查询课程的根结点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList == null || teachplanList.size() <= 0){
            //查询不到，要自动添加根结点
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();
    }

    /**
     * 添加课程基本信息
     * @param courseBase
     * @return
     */
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        String courseBaseId = courseBase.getId();
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBaseId);
    }

    /**
     * 根据课程id查询课程基本信息
     * @param courseId
     * @return
     */
    @Transactional
    public CourseBase getCourseBaseById(String courseId) {
        if (courseId == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()){
            CourseBase courseBase = optional.get();
            return courseBase;
        }
        return null;

    }

    /**
     * 更新课程基本信息
     * @param id
     * @param courseBase
     * @return
     */
    public ResponseResult updateCourseBase(String id, CourseBase courseBase) {
        CourseBase courseBaseNew = this.getCourseBaseById(id);

        BeanUtils.copyProperties(courseBase,courseBaseNew);
        courseBaseRepository.save(courseBaseNew);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 向课程管理数据库添加课程与图片的关联信息
     * @param courseId
     * @param pic
     * @return
     */
    public ResponseResult addCoursePic(String courseId, String pic) {
        CoursePic coursePic = null;

        //查询课程图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()){
            coursePic = optional.get();
        }
        if (coursePic == null){
            coursePic = new CoursePic();
        }

        coursePic.setPic(pic);
        coursePic.setCourseid(courseId);
        coursePicRepository.save(coursePic);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询课程图片
     * @param courseId
     * @return
     */
    public CoursePic findCoursePic(String courseId) {

        //查询课程图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    /**
     * 删除课程图片
     * @param courseId
     * @return
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result > 0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 查询课程视图，包括基本信息，图片，营销信息，课程计划
     * @param courseId
     * @return
     */
    public CourseView getCourseView(String courseId) {
        CourseView courseView = new CourseView();
        //查询课程基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
        if (courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程图片
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(courseId);
        if (coursePicOptional.isPresent()){
            CoursePic coursePic = coursePicOptional.get();
            courseView.setCoursePic(coursePic);
        }
        //课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(courseId);
        if (courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;


    }

    /**
     * 根据id查询课程基本信息
     * @param courseId
     * @return
     */
    public CourseBase findCourseBaseById(String courseId){
        //获取课程信息
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(courseId);
        if(!optionalCourseBase.isPresent()){
            //课程不存在抛出异常
            ExceptionCast.cast(CourseCode.COURSE_NOTEXIST);
            return null;
        }
        return optionalCourseBase.get();
    }

    /**
     * 课程预览
     * @param id
     * @return
     */
    public CoursePublishResult preview(String id) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);
        //请求cms添加页面
        //指标cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setDataUrl(publish_dataUrlPre + id);
        cmsPage.setPageName(id + ".html");
        cmsPage.setPageAliase(courseBaseById.getName());//页面别名就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);
        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if (!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //拼装页面预览的url
        String url = previewUrl + pageId;
        //返回CoursePublishResult对象(当中包括了页面预览的url
        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }

    /**
     * 课程发布
     * @param courseId
     * @return
     */
    @Transactional
    public CoursePublishResult publish(String courseId) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(courseId);

        //请求cms添加页面
        //准备cmsPage页面信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setDataUrl(publish_dataUrlPre + courseId);
        cmsPage.setPageName(courseId + ".html");
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);
        cmsPage.setPageAliase(courseBaseById.getName());//页面别名就是课程名称
        //调用cms一键发布接口将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        //保存课程的发布状态为“已发布”
        CourseBase courseBase = this.saveCoursePubState(courseId);
        if (courseBase == null){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //保存课程索引信息
        //先创建一个coursePub对象
        CoursePub coursePub = this.createCoursePub(courseId);

        //将coursePub对象保存到数据库
        this.saveCoursePub(courseId, coursePub);

        //缓存课程的信息

        //得到页面的url
        String pageUrl = cmsPostPageResult.getPageUrl();

        //向teachplanMediaPub中保存课程媒资信息
        saveTeachplanMediaPub(courseId);

        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);

    }

    /**
     * 向teachplanMediaPub中保存课程媒资信息
     * @param courseId
     */
    private void saveTeachplanMediaPub(String courseId){
        //先删除teachplanMediaPub中的数据
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //想teachplanMedia中查询
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        //将teachplanMediaList数据放到teachplanMediaPubs中
        for (TeachplanMedia teachplanMedia:teachplanMediaList){
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            //添加时间戳
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }
        //将teachplanMediaList插入到teachplanMediaPub
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }



    /**
     * 将coursePub对象保存到数据库
     * @param id 课程id
     * @param coursePub
     * @return
     */
    private CoursePub saveCoursePub(String id,CoursePub coursePub){

        CoursePub coursePubNew = null;
        //根据课程id查询coursePub
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if (coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }else {
            coursePubNew = new CoursePub();
        }
        //将coursePub对象中的信息保存到coursePubNew中
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        //时间戳，给logstach使用
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH-mm-ss");
        String data = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(data);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;

    }

    /**
     * 创建coursePub对象
     * @param id 课程id
     * @return
     */
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        //根据课程id查询course_base
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(id);
        if (baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            //将courseBase属性拷贝到coursePub中
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);

        //将课程计划信息json串保存到course_pub中
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }


    /**
     * 更新课程状态为已发布 202002
     * @param courseId
     * @return
     */
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBaseById = this.findCourseBaseById(courseId);
        courseBaseById.setStatus("202002");
        courseBaseRepository.save(courseBaseById);
        return courseBaseById;
    }

    /**
     * 保存课程计划与媒资文件的关联信息
     * @param teachplanMedia
     * @return
     */
    public ResponseResult saveTeachplanMedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_NOTEXIST);
        }
        Teachplan teachplan = optional.get();
        //只允许为叶子结点课程计划选择视频
        String grade = teachplan.getGrade();
        if(StringUtils.isEmpty(grade) || !grade.equals("3")){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //查询teachplanMedia
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if (mediaOptional.isPresent()){
            one = mediaOptional.get();
        }else {
            one = new TeachplanMedia();
        }
        //将one保存到数据库
        one.setCourseId(teachplan.getCourseid());//课程id
        one.setMediaId(teachplanMedia.getMediaId());//媒资文件的id
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件的原始名称
        one.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件的url
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);

    }
}





