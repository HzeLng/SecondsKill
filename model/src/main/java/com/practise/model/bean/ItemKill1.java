package com.practise.model.bean;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * @author HzeLng
 * @version 1.0
 * @description ItemKill1
 * @date 2020/12/2 15:42
 */
public class ItemKill1 {
    private Integer id;

    private Integer item_id;

    private Integer total;

    /**
     * @JsonFormat 是将数据库读取出的时间字段 格式化 方便前端正常显示
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date start_time;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date end_time;

    private Byte is_active;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date create_time;

    /**
     *
     */
    private  String itemName;

    private  Integer canKill;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public void setItem_id(Integer item_id) {
        this.item_id = item_id;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public Byte getIs_active() {
        return is_active;
    }

    public void setIs_active(Byte is_active) {
        this.is_active = is_active;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getCanKill() {
        return canKill;
    }

    public void setCanKill(Integer canKill) {
        this.canKill = canKill;
    }
}
