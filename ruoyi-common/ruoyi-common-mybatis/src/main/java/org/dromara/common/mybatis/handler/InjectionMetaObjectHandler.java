package org.dromara.common.mybatis.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.satoken.utils.LoginHelper;

import java.util.Date;

/**
 * MP注入处理器
 *
 * @author Lion Li
 * @date 2021/4/25
 */
@Slf4j
public class InjectionMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入填充方法，用于在执行插入操作时，自动填充实体类中的公共字段，如创建时间、更新时间、创建人、更新人等。
     * <p>
     * 该方法支持两种填充方式：
     * 1. 当实体类继承了 {@code BaseEntity} 时，通过访问实体类的属性直接填充。
     * 2. 当未继承 {@code BaseEntity} 时，使用 MyBatis-Plus 提供的 {@code strictInsertFill} 方法填充字段。
     * </p>
     *
     * <h3>功能描述</h3>
     * <ul>
     *     <li>填充创建时间（`createTime`）和更新时间（`updateTime`）。</li>
     *     <li>如果未设置创建人（`createBy`），自动获取当前登录用户作为创建人和更新人。</li>
     *     <li>填充创建部门（`createDept`），优先使用已有值，否则使用当前登录用户的部门 ID。</li>
     * </ul>
     *
     * <h3>参数</h3>
     * <ul>
     *     <li>{@code metaObject}：MyBatis-Plus 提供的元对象，包含要操作的实体对象和相关元数据。</li>
     * </ul>
     *
     * <h3>填充逻辑</h3>
     * <ol>
     *     <li>检查 {@code metaObject} 是否为 {@code BaseEntity} 类型。</li>
     *     <li>如果是：
     *         <ul>
     *             <li>获取当前时间，作为创建时间（`createTime`）和更新时间（`updateTime`）。</li>
     *             <li>如果创建人（`createBy`）为空，尝试从当前登录用户中获取用户 ID 和部门 ID，填充创建人、更新人和创建部门。</li>
     *         </ul>
     *     </li>
     *     <li>如果不是：
     *         <ul>
     *             <li>使用默认当前时间，填充 `createTime` 和 `updateTime`。</li>
     *         </ul>
     *     </li>
     *     <li>如果任何步骤中出现异常，将抛出自定义 {@code ServiceException}。</li>
     * </ol>
     *
     * <h3>异常处理</h3>
     * <ul>
     *     <li>如果填充过程中出现异常，例如获取登录用户失败或实体类型不匹配，将抛出 {@code ServiceException}，并记录错误信息。</li>
     * </ul>
     *
     * <h3>示例</h3>
     * <pre>
     * // 假设有一个继承了 BaseEntity 的实体类：
     * public class SysUser extends BaseEntity {
     *     private Long userId;
     *     private String username;
     * }
     *
     * // 插入操作时，自动填充如下字段：
     * createTime = 当前时间;
     * updateTime = 当前时间;
     * createBy   = 当前登录用户 ID;
     * updateBy   = 当前登录用户 ID;
     * createDept = 当前登录用户的部门 ID;
     * </pre>
     *
     * @param metaObject 元对象，用于获取待填充的实体对象。
     * @throws ServiceException 如果填充过程发生异常，将抛出此异常并返回错误信息。
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
                // 获取当前时间作为创建时间和更新时间，如果创建时间不为空，则使用创建时间，否则使用当前时间
                Date current = ObjectUtil.isNotNull(baseEntity.getCreateTime())
                    ? baseEntity.getCreateTime() : new Date();
                baseEntity.setCreateTime(current);
                baseEntity.setUpdateTime(current);

                // 如果创建人为空，则填充当前登录用户的信息
                if (ObjectUtil.isNull(baseEntity.getCreateBy())) {
                    LoginUser loginUser = getLoginUser();
                    if (ObjectUtil.isNotNull(loginUser)) {
                        Long userId = loginUser.getUserId();
                        // 填充创建人、更新人和创建部门信息
                        baseEntity.setCreateBy(userId);
                        baseEntity.setUpdateBy(userId);
                        baseEntity.setCreateDept(ObjectUtil.isNotNull(baseEntity.getCreateDept())
                            ? baseEntity.getCreateDept() : loginUser.getDeptId());
                    }
                }
            } else {
                Date date = new Date();
                this.strictInsertFill(metaObject, "createTime", Date.class, date);
                this.strictInsertFill(metaObject, "updateTime", Date.class, date);
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 更新填充方法，用于在执行更新操作时，自动填充实体类中的公共字段，如更新时间和更新人信息。
     * <p>
     * 该方法支持两种填充方式：
     * 1. 当实体类继承了 {@code BaseEntity} 时，通过访问实体类的属性直接填充。
     * 2. 当未继承 {@code BaseEntity} 时，使用 MyBatis-Plus 提供的 {@code strictUpdateFill} 方法填充字段。
     * </p>
     *
     * <h3>功能描述</h3>
     * <ul>
     *     <li>填充更新时间（`updateTime`），无论原始对象中的该字段是否已有值，都会覆盖为当前时间。</li>
     *     <li>填充更新人（`updateBy`），尝试获取当前登录用户的 ID，并将其设置为更新人。</li>
     * </ul>
     *
     * <h3>参数</h3>
     * <ul>
     *     <li>{@code metaObject}：MyBatis-Plus 提供的元对象，包含要操作的实体对象和相关元数据。</li>
     * </ul>
     *
     * <h3>填充逻辑</h3>
     * <ol>
     *     <li>检查 {@code metaObject} 是否为 {@code BaseEntity} 类型。</li>
     *     <li>如果是：
     *         <ul>
     *             <li>获取当前时间，设置为更新时间（`updateTime`）。</li>
     *             <li>获取当前登录用户的 ID，设置为更新人（`updateBy`）。</li>
     *         </ul>
     *     </li>
     *     <li>如果不是：
     *         <ul>
     *             <li>使用默认当前时间，填充 `updateTime` 字段。</li>
     *         </ul>
     *     </li>
     *     <li>如果任何步骤中出现异常，将抛出自定义 {@code ServiceException}。</li>
     * </ol>
     *
     * <h3>异常处理</h3>
     * <ul>
     *     <li>如果填充过程中出现异常，例如获取登录用户失败或实体类型不匹配，将抛出 {@code ServiceException}，并记录错误信息。</li>
     * </ul>
     *
     * <h3>示例</h3>
     * <pre>
     * // 假设有一个继承了 BaseEntity 的实体类：
     * public class SysUser extends BaseEntity {
     *     private Long userId;
     *     private String username;
     * }
     *
     * // 更新操作时，自动填充如下字段：
     * updateTime = 当前时间;
     * updateBy   = 当前登录用户 ID;
     * </pre>
     *
     * @param metaObject 元对象，用于获取待填充的实体对象。
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
                // 获取当前时间作为更新时间，无论原始对象中的更新时间是否为空都填充
                Date current = new Date();
                baseEntity.setUpdateTime(current);

                // 获取当前登录用户的ID，并填充更新人信息
                Long userId = LoginHelper.getUserId();
                if (ObjectUtil.isNotNull(userId)) {
                    baseEntity.setUpdateBy(userId);
                }
            } else {
                this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录用户的信息，如果用户未登录则返回 null
     */
    private LoginUser getLoginUser() {
        LoginUser loginUser;
        try {
            loginUser = LoginHelper.getLoginUser();
        } catch (Exception e) {
            log.warn("自动注入警告 => 用户未登录");
            return null;
        }
        return loginUser;
    }

}
