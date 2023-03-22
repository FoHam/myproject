package com.kh.myproduct.web.form;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SaveForm {
  @NotBlank // null 빈문자열("")을 허용 안함, 문자열 타입에만 사용
  @Size(min=2,max=10,message = "2~10자리 문자열")
  private String pname;
  @NotNull // 모든 타입에 대해 null 허용 안함
  @Positive //양수
  @Max(1000) //최대값
  private Long quantity;
  @NotNull
  @Positive
  @Min(1000) //최소값
  private Long price;
//  @NotEmpty null 빈문자열("") 공백문자(" ") 허용안함
//  문자열,컬렉션타입(요소가 1개이상 존재)에 사용
}