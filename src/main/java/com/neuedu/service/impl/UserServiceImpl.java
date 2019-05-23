package com.neuedu.service.impl;

import com.neuedu.common.Constant;
import com.neuedu.common.RoleEnum;
import com.neuedu.common.ServerResponse;

import com.neuedu.dao.UserMapper;
import com.neuedu.pojo.User;
import com.neuedu.service.IUserService;
import com.neuedu.utils.MD5Utils;
import com.neuedu.utils.TokenCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {


    @Autowired
    private UserMapper userMapper;


    @Override
    public ServerResponse login(String username, String password) {

        //step1:参数的非空检验
        if (username==null||username.equals("")){
            return ServerResponse.serverResponseByError("用户名不能为空");
        }
        if (password==null||password.equals("")){
            return ServerResponse.serverResponseByError("密码不能为空");
        }
        //step2：检查用户名是否存在
        if (userMapper.checkUsername(username)==0){
            return ServerResponse.serverResponseByError(Constant.USERNAME_NOT_EXIST,"用户名不存在");
        }
        //step3：根据用户名和密码查找用户信息

        User result = userMapper.selectUserByUsernameAndPassword(username,MD5Utils.getMD5Code(password));
        if (result==null){
            return ServerResponse.serverResponseByError("密码错误1111");

        }
        //step4：返回结果

        result.setPassword("");

        return ServerResponse.serverResponseBySuccess(result);
    }

    @Override
    public ServerResponse register(User user) {

        //step1：参数校验
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }

        //step2:检测用户名是否存在
        if (userMapper.checkUsername(user.getUsername())>0){
            return ServerResponse.serverResponseByError(Constant.USERNAME_EXISTS,"用户名已存在");
        }
        //step3：检测邮箱是否存在
        if (userMapper.checkEmail(user.getEmail())>0){
            return ServerResponse.serverResponseByError(Constant.EMAIL_EXISTS,"邮箱已存在");
        }

        //step4：MD5加密密码   -->MD5加密是不可逆的加密，相对比较安全
        user.setPassword(MD5Utils.getMD5Code(user.getPassword()));
        //step5:设置用户角色
        user.setRole(RoleEnum.ROLE_USER.getRole());
        //step6：进行注册
        if (userMapper.insert(user)<=0){
            return ServerResponse.serverResponseByError(Constant.EXISTS_FAIL,"注册失败");
        }


        return ServerResponse.serverResponseBySuccess();
    }

    @Override
    public ServerResponse admin_register(User user) {

        //step1：参数校验
        if (user==null){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }

        //step2:检测用户名是否存在
        if (userMapper.checkUsername(user.getUsername())>0){
            return ServerResponse.serverResponseByError(Constant.USERNAME_EXISTS,"用户名已存在");
        }
        //step3：检测邮箱是否存在
        if (userMapper.checkEmail(user.getEmail())>0){
            return ServerResponse.serverResponseByError(Constant.EMAIL_EXISTS,"邮箱已存在");
        }
        //step4：MD5加密密码   -->MD5加密是不可逆的加密，相对比较安全
        user.setPassword(MD5Utils.getMD5Code(user.getPassword()));
        //step5:设置用户角色
        user.setRole(RoleEnum.ROLE_ADMIN.getRole());
        //step6：进行注册
        if (userMapper.insert(user)<=0){
            return ServerResponse.serverResponseByError(Constant.EXISTS_FAIL,"注册失败");
        }


        return ServerResponse.serverResponseBySuccess();


    }

    @Override
    public ServerResponse forget_get_question(String username) {

        //step1：参数非空判断
        if (username==null||username.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        //step2:判断用户名是否存在
        if (userMapper.checkUsername(username)==0){
            return ServerResponse.serverResponseByError(Constant.USERNAME_NOT_EXIST,"用户名不存在");
        }
        //step3：查找密保问题
        String question = userMapper.selectQuestionByUsername(username);
        if (question==null||question.equals("")){
            return ServerResponse.serverResponseByError(Constant.QUESTION_ISNULL,"密保问题为空");
        }

        return ServerResponse.serverResponseBySuccess(question);
    }

    @Override
    public ServerResponse forget_check_answer(String username, String question, String answer) {

        //step1:参数非空判断
        if (username==null||username.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        if (question==null||question.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        if (answer==null||answer.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        //step2:根据username，question，answer查询
        int result = userMapper.selecByUsernameAndQuestionAndAnswer(username, question, answer);
        if (result==0){
            return ServerResponse.serverResponseByError("答案错误");
        }
        //step3：服务器生成一个tocken保存，并将tocken返回给客户端
        String tocken = UUID.randomUUID().toString();

        TokenCache.set(username,tocken);

        return ServerResponse.serverResponseBySuccess(tocken);
    }

    @Override
    public ServerResponse forget_reset_password(String username, String passwordNew, String forgetToken) {

        //step1:参数非空校验
        if (username==null||username.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        if (passwordNew==null||passwordNew.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        if (forgetToken==null||forgetToken.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        //step2：token校验
        if (forgetToken==null){
            return ServerResponse.serverResponseByError("token已过期");
        }
        String token = TokenCache.get(username);
        if (!token.equals(forgetToken)){
            return ServerResponse.serverResponseByError("无效的token");
        }
        //修改密码
        int result = userMapper.updatePasswordByUsername(username, MD5Utils.getMD5Code(passwordNew));
        if (result>0){
            return ServerResponse.serverResponseBySuccess(Constant.CHANGE_PASSWORD_SUCCESS,"修改密码成功");
        }
        return ServerResponse.serverResponseByError("密码修改失败");
    }

    @Override
    public ServerResponse check_valid(String str, String type) {

        //step1:参数非空校验
        if (str==null||str.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        if (type==null||type.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        //step2:根据type类型 检查对应的是username还是email
        if (type.equals("username")){
            int result = userMapper.checkUsername(str);
            if (result>0){
                return ServerResponse.serverResponseByError(Constant.USERNAME_EXISTS,"用户名已存在");
            }
            return ServerResponse.serverResponseBySuccess("用户名可以使用");
        }else if (type.equals("email")){
            int result = userMapper.checkEmail(str);
            if (result>0){
                return ServerResponse.serverResponseByError(Constant.EMAIL_EXISTS,"邮箱已存在");
            }
            return ServerResponse.serverResponseBySuccess("邮箱可以使用");
        }else {
            return ServerResponse.serverResponseByError(Constant.PARAM_TYPE_ERROR,"参数类型错误,请输入规定的参数类型");
        }

    }

    @Override
    public ServerResponse reset_password(String username,String passwordOld, String passwordNew) {

        //step1:参数非空校验
        if (username==null||username.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        if (passwordOld==null||passwordOld.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }
        if (passwordNew==null||passwordNew.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }

        //step2：判断老密码是否正确
        User user = userMapper.selectUserByUsernameAndPassword(username, MD5Utils.getMD5Code(passwordOld));
        if (user==null){
            return ServerResponse.serverResponseByError("旧密码错误");
        }

        //stpe3:修改密码
        user.setPassword(MD5Utils.getMD5Code(passwordNew));
        int result = userMapper.updateByPrimaryKey(user);
        if (result>0){
            return ServerResponse.serverResponseBySuccess("修改密码成功");
        }

        return ServerResponse.serverResponseByError("修改密码失败");
    }

    @Override
    public ServerResponse update_information(User userInfo) {

        //step1：判断参数是否为空
        if (userInfo==null||userInfo.equals("")){
            return ServerResponse.serverResponseByError(Constant.PARAM_NOT_NULL,"参数不能为空");
        }

        //step2：更新用户信息
        int result = userMapper.updateUserByExistenceField(userInfo);
        if (result>0){
            return ServerResponse.serverResponseBySuccess("更改个人信息成功");
        }

        return ServerResponse.serverResponseByError("更改个人信息失败");

    }

    @Override
    public User findUserByUserId(Integer userId) {
        return userMapper.selectByPrimaryKey(userId);
    }
}
