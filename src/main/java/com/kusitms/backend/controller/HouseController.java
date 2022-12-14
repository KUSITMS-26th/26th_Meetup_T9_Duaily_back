package com.kusitms.backend.controller;

import com.kusitms.backend.config.SecurityUtil;
import com.kusitms.backend.domain.HousePost;
import com.kusitms.backend.dto.DealDto;
import com.kusitms.backend.dto.HouseDto;
import com.kusitms.backend.dto.HousePreviewDto;
import com.kusitms.backend.response.BaseResponse;
import com.kusitms.backend.response.PageInfo;
import com.kusitms.backend.response.PageResponse;
import com.kusitms.backend.service.IHouseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/house")
public class HouseController {

  private final IHouseService houseService;

  @GetMapping("/list")
  public ResponseEntity<PageResponse> getHousePostList(
      @RequestParam(name = "page", defaultValue = "1") Integer page
  ) {
    int size = 8;
    int totalElements = houseService.getHousePostCount();
    int totalPages = totalElements / size + 1;

    List<HousePreviewDto> response = houseService.getHousePostList(PageRequest.of(page - 1, size));

    PageInfo pageInfo = PageInfo.builder().page(page).size(size)
        .totalElements(totalElements).totalPages(totalPages)
        .build();

    return ResponseEntity.ok(PageResponse.builder().message("빈 집 게시글 목록 조회를 성공하셨습니다.")
        .data(response).pageInfo(pageInfo).build());
  }

  @PostMapping
  public ResponseEntity<BaseResponse> create(@RequestBody HouseDto.Request request) {

    Long response = houseService.create(SecurityUtil.getCurrentUserId(), request);
    return ResponseEntity.ok(BaseResponse.builder()
        .message("빈 집 게시글 생성 성공").data(response).build());
  }

  @PostMapping("/deal")
  public ResponseEntity<BaseResponse> createDeal(@RequestBody DealDto.Request request) {

    Long response = houseService.createDeal(request, SecurityUtil.getCurrentUserId());
    return ResponseEntity.ok(BaseResponse.builder()
        .data(response).message("거래가 성공적으로 시작되었습니다.").build());
  }

  @PutMapping("/deal/{dealId}")
  public ResponseEntity<BaseResponse> modifyDeal(@PathVariable Long dealId) {

    Long response = houseService.modifyDeal(dealId, SecurityUtil.getCurrentUserId());
    return ResponseEntity.ok(BaseResponse.builder()
        .data(response).message("거래가 성공적으로 완료되었습니다.").build());
  }

  @GetMapping("/{houseId}")
  public ResponseEntity<BaseResponse> getDetail(@PathVariable Long houseId) {
    HouseDto.Response response = houseService.getDetail(houseId);
    return ResponseEntity.ok(BaseResponse.builder()
        .data(response).message("상세 조회 성공").build());
  }

  @GetMapping("/mine")
  public ResponseEntity<PageResponse> getMineList(
      @RequestParam(name = "page", defaultValue = "1") Integer page) {

    PageResponse response =
        houseService.getMineList(SecurityUtil.getCurrentUserId(),
            PageRequest.of(page - 1, 8));

    response.setMessage("본인이 작성한 게시글 조회 성공");

    return ResponseEntity.ok(response);
  }
}
