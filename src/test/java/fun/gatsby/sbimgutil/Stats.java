package fun.gatsby.sbimgutil;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
public class Stats {

    private Long id;

    /**
     * 业务日期
     */
    private LocalDate businessDate;

    /**
     * 年月
     */
    private String yearMonth;

    /**
     * 客户id
     */
    private Long customerId;
    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 合同id
     */
    private Long contractId;
    /**
     * 汇总类型: 0日汇, 1月汇
     */
    private Integer kind;
    /**
     * 部门类型:1第三方或工地,0其他
     */
    private Integer orgType;
    /**
     * 产值
     */
    private BigDecimal output;
    /**
     * 结算
     */
    private BigDecimal settle;
    /**
     * 开票
     */
    private BigDecimal invoice;
    /**
     * 回款
     */
    private BigDecimal payment;
    /**
     * 预计成本 检测所成本(签单额 * 比例) 或 原材工时价
     */
    private BigDecimal expectCost;
    /**
     * 实际成本 检测所成本(结算 * 比例) 或 原材工时价
     */
    private BigDecimal actualCost;
    /**
     * 成本 (弃)
     */
    private BigDecimal cost;
    /**
     * 项目数量
     */
    private Integer projectCount;
    /**
     * 利润 = 结算 - 实际成本
     */
    private BigDecimal profit;
    /**
     * 利润率 = 利润 / 结算
     */
    private BigDecimal profitRate;

    /**
     * 待回款 = 总结算 - 总回款 (实时)
     */
    private BigDecimal waitPayment;


    private List<Stats> children;

    /**
     * 合同名称
     */
    private String contractName;

    /**
     * 合同编号
     */
    private String contractCode;

    /**
     * 业务员
     */
    private Long signBy;

    /**
     * 负责人
     */
    private String director;
}
