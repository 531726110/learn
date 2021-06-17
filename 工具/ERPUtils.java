package com.boco.scms.connector.osberp.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;

import com.boco.framework.exception.BusinessException;

public class ERPUtils {
    public static HashMap erpErrorCode = new HashMap();
    
    ERPUtils() {
        // 2. ESB_RETURN_CODE ESB 返回代码VARCHAR2(50) ESB 侧执行结果为False
        // 时，必须填写错误代码，错误代码包括：
        // ESB-01 服务调用超时
        // ESB-02 服务请求和WSDL 规范不匹配
        // ESB-03 服务IP 地址未授权
        // ESB-04 服务安全校验不通过
        // ESB-05 ESB 总线内部异常
        erpErrorCode.put("ESB-01", "服务调用超时");
        erpErrorCode.put("ESB-02", "服务请求和WSDL规范不匹配");
        erpErrorCode.put("ESB-03", "服务IP地址未授权");
        erpErrorCode.put("ESB-04", "服务安全校验不通过");
        erpErrorCode.put("ESB-05", "ESB总线内部异常");
        
        // 5. BIZ_RETURN_CODE 业务服务返回代码 VARCHAR2(30) 业务服务侧执行结果为False
        // 时，必须填写错误代码，错误代码包括：
        // BIZ-01 服务请求输入完整性校验不通过
        // BIZ-02 服务业务规则处理不通过
        // BIZ-03 业务系统处理超时
        // BIZ-04 业务系统其它处理异常
        erpErrorCode.put("BIZ-01", "服务请求输入完整性校验不通过");
        erpErrorCode.put("BIZ-02", "服务业务规则处理不通过");
        erpErrorCode.put("BIZ-03", "业务系统处理超时");
        erpErrorCode.put("BIZ-04", "业务系统其它处理异常");
    }
    
    /**
     * 2.ESB_RETURN_CODE ESB 返回代码VARCHAR2(50) ESB 侧执行结果为False
     * 业务服务侧执行结果为False时，必须填写错误代码，错误代码包括：<br>
     * ESB-01 服务调用超时 <br>
     * ESB-02 服务请求和WSDL规范不匹配 <br>
     * ESB-03 服务IP地址未授权 <br>
     * ESB-04 服务安全校验不通过 <br>
     * ESB-05 ESB总线内部异常
     * 
     * @param ESB_RETURN_CODE
     * @return
     */
    public String getESB_RETURN_CODE(String ESB_RETURN_CODE) {
        
        if (erpErrorCode.get(ESB_RETURN_CODE) != null) {
            return ESB_RETURN_CODE + ":"
                   + (String) erpErrorCode.get(ESB_RETURN_CODE);
        }
        return "";
    }
    
    /**
     * 5. BIZ_RETURN_CODE 业务服务返回代码 VARCHAR2(30)
     * 业务服务侧执行结果为False时，必须填写错误代码，错误代码包括：<br>
     * BIZ-01 服务请求输入完整性校验不通过<br>
     * BIZ-02 服务业务规则处理不通过 <br>
     * BIZ-03 业务系统处理超时 <br>
     * BIZ-04 业务系统其它处理异常
     * 
     * @return
     */
    public String getBIZ_RETURN_CODE(String BIZ_RETURN_CODE) {
        if (erpErrorCode.get(BIZ_RETURN_CODE) != null) {
            return BIZ_RETURN_CODE + ":"
                   + (String) erpErrorCode.get(BIZ_RETURN_CODE);
        }
        return "";
    }


    public static void copyPropertiesToParent(Object source, Object target) throws BusinessException {
        try {
            Class sourceClazz = source.getClass();
            Class targetClazz = target.getClass();
            Field[] sourceFields = sourceClazz.getDeclaredFields();
            Field[] targetFields = targetClazz.getSuperclass().getDeclaredFields();
            for (int i = 0; i < sourceFields.length; i++) {
                Field sourceField = sourceFields[i];
                PropertyDescriptor pdSource = new PropertyDescriptor(sourceField.getName()
                        .toUpperCase(),
                        sourceClazz);
                Method rM = pdSource.getReadMethod();
                Object value = rM.invoke(source);
                if (value != null) {
                    for (int j = 0; j < targetFields.length; j++) {
                        Field targetField = targetFields[j];
                        if (sourceField.getName()
                                .replaceAll("_", "")
                                .toLowerCase()
                                .equals(targetField.getName()
                                        .replaceAll("_", "")
                                        .toLowerCase())) {
                            PropertyDescriptor pdTarget = new PropertyDescriptor(targetField.getName()
                                    .toUpperCase(),
                                    targetClazz.getSuperclass());
                            Method wM = pdTarget.getWriteMethod();
                            if ("class javax.xml.datatype.XMLGregorianCalendar".equals(sourceField.getType()
                                    .toString())) {
                                if ("class java.util.Date".equals(targetField.getType()
                                        .toString())) {
                                    wM.invoke(target,
                                            ((XMLGregorianCalendar) value).toGregorianCalendar()
                                                    .getTime());
                                }
                                else if ("class javax.xml.datatype.XMLGregorianCalendar".equals(targetField.getType()
                                        .toString())) {
                                    wM.invoke(target,
                                            (XMLGregorianCalendar) value);
                                }
                            }
                            else if ("class java.math.BigDecimal".equals(sourceField.getType()
                                    .toString())) {
                                if ("class java.lang.Long".equals(targetField.getType()
                                        .toString())) {
                                    wM.invoke(target,
                                            ((BigDecimal) value).longValue());
                                }
                                else if ("class java.math.BigDecimal".equals(targetField.getType()
                                        .toString())) {
                                    wM.invoke(target, (BigDecimal) value);
                                }
                                else if ("class java.lang.Double".equals(targetField.getType()
                                        .toString())) {
                                    wM.invoke(target,
                                            Double.valueOf(value.toString()));
                                }
                            }
                            else if (sourceField.getType()
                                    .toString()
                                    .equals(targetField.getType()
                                            .toString())) {
                                wM.invoke(target, value);
                            }
                            else if ("class java.lang.Double".equals(sourceField.getType()
                                    .toString())) {
                                if ("class java.math.BigDecimal".equals(targetField.getType()
                                        .toString())) {
                                    wM.invoke(target,
                                            (BigDecimal.valueOf((Double) value)));
                                }
                                else {
                                    wM.invoke(target, (Double) value);
                                }
                            }
                            else if ("class java.lang.Long".equals(sourceField.getType()
                                    .toString())) {
                                if ("class java.math.BigDecimal".equals(targetField.getType()
                                        .toString())) {
                                    wM.invoke(target,
                                            new BigDecimal((Long) value));
                                }
                                else {
                                    wM.invoke(target, (Long) value);
                                }
                            }
                            else if ("class java.util.Date".equals(sourceField.getType()
                                    .toString())) {
                                if ("class java.util.Date".equals(targetField.getType()
                                        .toString())) {
                                    wM.invoke(target, (Date) value);
                                }
                                else if ("class javax.xml.datatype.XMLGregorianCalendar".equals(targetField.getType()
                                        .toString())) {
                                    wM.invoke(target,
                                            DateUtils.convertToXMLGregorianCalendar((Date) value));
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 将接口类的值复制到业务类中
     * 
     * @param source
     *            接口类
     * @param target
     *            业务类
     * @throws Exception
     */
    public static void copyProperties(Object source, Object target) throws BusinessException {
        try {
            Class sourceClazz = source.getClass();
            Class targetClazz = target.getClass();
            Field[] sourceFields = sourceClazz.getDeclaredFields();
            Field[] targetFields = targetClazz.getDeclaredFields();
            for (int i = 0; i < sourceFields.length; i++) {
                Field sourceField = sourceFields[i];
                PropertyDescriptor pdSource = new PropertyDescriptor(sourceField.getName()
                                                                                .toUpperCase(),
                                                                     sourceClazz);
                Method rM = pdSource.getReadMethod();
                Object value = rM.invoke(source);
                if (value != null) {
                    for (int j = 0; j < targetFields.length; j++) {
                        Field targetField = targetFields[j];
                        if (sourceField.getName()
                                       .replaceAll("_", "")
                                       .toLowerCase()
                                       .equals(targetField.getName()
                                                          .replaceAll("_", "")
                                                          .toLowerCase())) {
                            PropertyDescriptor pdTarget = new PropertyDescriptor(targetField.getName()
                                                                                            .toUpperCase(),
                                                                                 targetClazz);
                            Method wM = pdTarget.getWriteMethod();
                            if ("class javax.xml.datatype.XMLGregorianCalendar".equals(sourceField.getType()
                                                                                                  .toString())) {
                                if ("class java.util.Date".equals(targetField.getType()
                                                                             .toString())) {
                                    wM.invoke(target,
                                              ((XMLGregorianCalendar) value).toGregorianCalendar()
                                                                            .getTime());
                                }
                                else if ("class javax.xml.datatype.XMLGregorianCalendar".equals(targetField.getType()
                                                                                                           .toString())) {
                                    wM.invoke(target,
                                              (XMLGregorianCalendar) value);
                                }
                            }
                            else if ("class java.math.BigDecimal".equals(sourceField.getType()
                                                                                    .toString())) {
                                if ("class java.lang.Long".equals(targetField.getType()
                                                                             .toString())) {
                                    wM.invoke(target,
                                              ((BigDecimal) value).longValue());
                                }
                                else if ("class java.math.BigDecimal".equals(targetField.getType()
                                                                                        .toString())) {
                                    wM.invoke(target, (BigDecimal) value);
                                }
                                else if ("class java.lang.Double".equals(targetField.getType()
                                                                                    .toString())) {
                                    wM.invoke(target,
                                              Double.valueOf(value.toString()));
                                }
                            }
                            else if (sourceField.getType()
                                                .toString()
                                                .equals(targetField.getType()
                                                                   .toString())) {
                                wM.invoke(target, value);
                            }
                            else if ("class java.lang.Double".equals(sourceField.getType()
                                                                                .toString())) {
                                if ("class java.math.BigDecimal".equals(targetField.getType()
                                                                                   .toString())) {
                                    wM.invoke(target,
                                              (BigDecimal.valueOf((Double) value)));
                                }
                                else {
                                    wM.invoke(target, (Double) value);
                                }
                            }
                            else if ("class java.lang.Long".equals(sourceField.getType()
                                                                              .toString())) {
                                if ("class java.math.BigDecimal".equals(targetField.getType()
                                                                                   .toString())) {
                                    wM.invoke(target,
                                              new BigDecimal((Long) value));
                                }
                                else {
                                    wM.invoke(target, (Long) value);
                                }
                            }
                            else if ("class java.util.Date".equals(sourceField.getType()
                                                                              .toString())) {
                                if ("class java.util.Date".equals(targetField.getType()
                                                                             .toString())) {
                                    wM.invoke(target, (Date) value);
                                }
                                else if ("class javax.xml.datatype.XMLGregorianCalendar".equals(targetField.getType()
                                                                                                           .toString())) {
                                    wM.invoke(target,
                                              DateUtils.convertToXMLGregorianCalendar((Date) value));
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        }
    }
    
    /**
     * 将request中和target属性相同参数的值设置入target中
     * 
     * @param target
     * @param request
     * @throws BusinessException
     */
    public static void setProperties(Object target, HttpServletRequest request) throws BusinessException {
        try {
            Class targetClazz = target.getClass();
            Field[] targetFields = targetClazz.getDeclaredFields();
            for (int i = 0; i < targetFields.length; i++) {
                Field targetField = targetFields[i];
                String value = request.getParameter(targetField.getName());
                if (StringUtils.isNotBlank(value)) {
                    value = URLDecoder.decode(value, "utf-8");
                    PropertyDescriptor pdTarget = new PropertyDescriptor(targetField.getName(),
                                                                         targetClazz);
                    Method wM = pdTarget.getWriteMethod();
                    if ("class java.lang.Double".equals(targetField.getType()
                                                                   .toString())) {
                        wM.invoke(target, Double.valueOf(value));
                    }
                    else if ("class java.lang.Long".equals(targetField.getType()
                                                                      .toString())) {
                        wM.invoke(target, Long.valueOf(value));
                    }
                    else if ("class java.util.Date".equals(targetField.getType()
                                                                      .toString())) {
                        wM.invoke(target,
                                  DateUtils.stringToDate(value, "yyyy-MM-dd"));
                    }
                    else if ("class java.lang.String".equals(targetField.getType()
                                                                        .toString())) {
                        wM.invoke(target, value);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        }
    }
    
    // 1 E 1 工程物资 1 工程物资
    // 2 C 2 业务用品 1 业务用品
    // 3 P 3 备品备件 1 备品备件
    // 4 O 4 办公用品 1 办公用品
    // 5 M 4 营销终端 1 营销终端
    // 6 Z 4 通用产品库 0 产品库
    // 7 W 5 网络物资 1 网络物资
    // 8 L 1 零购 1 零购
    // 9 G 1 工程网络物资 1 工程网络物资
    // 10 W 4 网络物资(信息化) 1 网络物资(信息化)
    // 101 F 4 废旧物资 1 废旧物资
    // 102 V 4 VOI物资 1 VOI物资
    // 103 B 4 暂存物资库 1 暂存物资
    // 104 Y 6 杂品 1 杂品
    // 105 A 7 报废物资处置申请 1 报废蓄电池
    // 106 D 8 报废物资处置申请 1 报废网络物资
    // 107 H 9 报废物资处置申请 1 其他
    // 1000 3 X 4 混合物资库（虚拟） 0 混合物资
    /**
     * IO库存组织 ; ERP标准物资类型 ; 辽宁物资类型<br>
     * E ; 工程物资 ; 工程物资、零购、工程网络物资<br>
     * P ; 备品备件 ; 备品备件、网络物资、网络物资（信息化）<br>
     * C ; 业务用品 ; 业务用品、营销终端 <br>
     * O ; 杂品 ; 办公用品
     * 
     * @param materialCateId
     * @param materialCateName
     * @throws BusinessException
     */
    public static String getMaterialCateERPName(Long materialCateId,
                                                String materialCateName) throws BusinessException {
        // 1 E 1 工程物资 1 工程物资
        // 8 L 1 零购 1 零购
        // 9 G 1 工程网络物资 1 工程网络物资
        if ((materialCateId == 1 || "工程物资".equals(materialCateName)) || (materialCateId == 8 || "零购 ".equals(materialCateName))
            || (materialCateId == 9 || "工程网络物资".equals(materialCateName))) {
            return "工程物资 ";
        }
        // 3 P 3 备品备件 1 备品备件
        // 7 W 5 网络物资 1 网络物资
        // 10 W 4 网络物资(信息化) 1 网络物资(信息化)
        if ((materialCateId == 3 || "备品备件".equals(materialCateName)) || (materialCateId == 7 || "网络物资 ".equals(materialCateName))
            || (materialCateId == 10 || "网络物资(信息化)".equals(materialCateName))) {
            return "备品备件";
        }
        
        // 2 C 2 业务用品 1 业务用品
        // 5 M 4 营销终端 1 营销终端
        if ((materialCateId == 2 || "业务用品 ".equals(materialCateName)) || (materialCateId == 5 || "营销终端 ".equals(materialCateName))) {
            return "业务用品  ";
        }
        // 4 O 4 办公用品 1 办公用品
        // 104 Y 6 杂品 1 杂品
        if ((materialCateId == 4 || "办公用品".equals(materialCateName)) || (materialCateId == 104 || "杂品 ".equals(materialCateName))) {
            return "杂品 ";
        }
        return "";
    }
    
    /**
     * IO库存组织 ; ERP标准物资类型 ; 辽宁物资类型<br>
     * E ; 工程物资 ; 工程物资、零购、工程网络物资<br>
     * P ; 备品备件 ; 备品备件、网络物资、网络物资（信息化）<br>
     * C ; 业务用品 ; 业务用品、营销终端 <br>
     * O ; 杂品 ; 办公用品
     * 
     * @param target
     * @param request
     * @throws BusinessException
     */
    public static String getMaterialCateERPCode(Long materialCateId,
                                                String materialCateName) throws BusinessException {
        // 1 E 1 工程物资 1 工程物资
        // 8 L 1 零购 1 零购
        // 9 G 1 工程网络物资 1 工程网络物资
        if ((materialCateId == 1 || "工程物资".equals(materialCateName)) || (materialCateId == 8 || "零购 ".equals(materialCateName))
            || (materialCateId == 9 || "工程网络物资".equals(materialCateName))) {
            return "E";
        }
        // 3 P 3 备品备件 1 备品备件
        // 7 W 5 网络物资 1 网络物资
        // 10 W 4 网络物资(信息化) 1 网络物资(信息化)
        if ((materialCateId == 3 || "备品备件".equals(materialCateName)) || (materialCateId == 7 || "网络物资 ".equals(materialCateName))
            || (materialCateId == 10 || "网络物资(信息化)".equals(materialCateName))) {
            return "P";
        }
        
        // 2 C 2 业务用品 1 业务用品
        // 5 M 4 营销终端 1 营销终端
        if ((materialCateId == 2 || "业务用品 ".equals(materialCateName)) || (materialCateId == 5 || "营销终端 ".equals(materialCateName))) {
            return "C";
        }
        // 4 O 4 办公用品 1 办公用品
        // 104 Y 6 杂品 1 杂品
        if ((materialCateId == 4 || "办公用品".equals(materialCateName)) || (materialCateId == 104 || "杂品 ".equals(materialCateName))) {
            return "O";
        }
        return "";
    }
}
