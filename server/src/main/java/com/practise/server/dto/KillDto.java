package com.practise.server.dto;


import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author HzeLng
 * @version 1.0
 * @description KillDto
 * @date 2020/12/2 22:05
 */
@Data
@ToString
public class KillDto implements Serializable {
    @NotNull
    private Integer killId;

    private Integer userId;


}
