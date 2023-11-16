package com.asset.sync.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("asset_info")
public class AssetInfoEntity {

    @TableId
    private String id;

    private String fields;

    private String ip;

    private String assetName;

    private String assetType;

    private String assetSubType;

    private String orgId;

    private String businessId;

    private String owner;

    private String safeStatus;

    private String surviveStatus;

    private Date createTime;

    private Date updateTime;

    private String isIntranet;

    private String isExpose;

    private String isFingerChange;

    private String createUser;

    private String updateUser;

    private Integer delFlag;

    private String ownerName;

    private String probeId;

    private String orgName;

    private String businessName;

    private String isOwnerAsset;

    private String bizKey;

    private String listenPortCount;

    private String agentId;

    private String tileFields;

    private String ipOrUrl;

    private String belongCity;

    private String webProtocol;

    private String middlewareType;

    private String hostOperatingSystemType;

    private String hostOperatingSystemName;

    private String databaseType;

    private String manufacturer;

    private String assetId;

    private Integer serialNumber;

    private String reportDataIsOk;

    private String fieldsErrorDetail;

    private String extraInfo;

    private String gradeObjectName;

    private String networkUnitType;

}
