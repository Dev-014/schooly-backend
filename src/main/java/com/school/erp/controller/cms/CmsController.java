package com.school.erp.controller.cms;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.cms.*;
import com.school.erp.service.cms.CmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CmsController {

    private final CmsService cmsService;

    // ─────────────────────────────────────────────────────────────────────────────
    // 1. EVENTS
    // ─────────────────────────────────────────────────────────────────────────────

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<List<CmsEventDto>>> getAllEvents(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getAllEvents(schoolId, search),
                "Events retrieved successfully"
        ));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<ApiResponse<CmsEventDto>> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getEventById(id),
                "Event details retrieved successfully"
        ));
    }

    @PostMapping("/events")
    public ResponseEntity<ApiResponse<CmsEventDto>> createEvent(@Valid @RequestBody CmsEventDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.createEvent(dto),
                "Event created successfully"
        ));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<ApiResponse<CmsEventDto>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody CmsEventDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.updateEvent(id, dto),
                "Event updated successfully"
        ));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        cmsService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Event deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2. GALLERIES
    // ─────────────────────────────────────────────────────────────────────────────

    @GetMapping("/galleries")
    public ResponseEntity<ApiResponse<List<CmsGalleryDto>>> getAllGalleries(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getAllGalleries(schoolId, search),
                "Galleries retrieved successfully"
        ));
    }

    @GetMapping("/galleries/{id}")
    public ResponseEntity<ApiResponse<CmsGalleryDto>> getGalleryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getGalleryById(id),
                "Gallery details retrieved successfully"
        ));
    }

    @PostMapping("/galleries")
    public ResponseEntity<ApiResponse<CmsGalleryDto>> createGallery(@Valid @RequestBody CmsGalleryDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.createGallery(dto),
                "Gallery created successfully"
        ));
    }

    @PutMapping("/galleries/{id}")
    public ResponseEntity<ApiResponse<CmsGalleryDto>> updateGallery(
            @PathVariable Long id,
            @Valid @RequestBody CmsGalleryDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.updateGallery(id, dto),
                "Gallery updated successfully"
        ));
    }

    @DeleteMapping("/galleries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGallery(@PathVariable Long id) {
        cmsService.deleteGallery(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Gallery deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3. NOTICES
    // ─────────────────────────────────────────────────────────────────────────────

    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<List<CmsNoticeDto>>> getAllNotices(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getAllNotices(schoolId, search),
                "Notices retrieved successfully"
        ));
    }

    @GetMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<CmsNoticeDto>> getNoticeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getNoticeById(id),
                "Notice details retrieved successfully"
        ));
    }

    @PostMapping("/notices")
    public ResponseEntity<ApiResponse<CmsNoticeDto>> createNotice(@Valid @RequestBody CmsNoticeDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.createNotice(dto),
                "Notice created successfully"
        ));
    }

    @PutMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<CmsNoticeDto>> updateNotice(
            @PathVariable Long id,
            @Valid @RequestBody CmsNoticeDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.updateNotice(id, dto),
                "Notice updated successfully"
        ));
    }

    @DeleteMapping("/notices/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        cmsService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notice deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4. PAGES
    // ─────────────────────────────────────────────────────────────────────────────

    @GetMapping("/pages")
    public ResponseEntity<ApiResponse<List<CmsPageDto>>> getAllPages(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getAllPages(schoolId, search),
                "Pages retrieved successfully"
        ));
    }

    @GetMapping("/pages/{id}")
    public ResponseEntity<ApiResponse<CmsPageDto>> getPageById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getPageById(id),
                "Page details retrieved successfully"
        ));
    }

    @PostMapping("/pages")
    public ResponseEntity<ApiResponse<CmsPageDto>> createPage(@Valid @RequestBody CmsPageDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.createPage(dto),
                "Page created successfully"
        ));
    }

    @PutMapping("/pages/{id}")
    public ResponseEntity<ApiResponse<CmsPageDto>> updatePage(
            @PathVariable Long id,
            @Valid @RequestBody CmsPageDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.updatePage(id, dto),
                "Page updated successfully"
        ));
    }

    @DeleteMapping("/pages/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePage(@PathVariable Long id) {
        cmsService.deletePage(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Page deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 5. MENUS
    // ─────────────────────────────────────────────────────────────────────────────

    @GetMapping("/menus")
    public ResponseEntity<ApiResponse<List<CmsMenuDto.MenuGroupDto>>> getAllMenus(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getAllMenus(schoolId, search),
                "Menus retrieved successfully"
        ));
    }

    @GetMapping("/menus/{id}")
    public ResponseEntity<ApiResponse<CmsMenuDto.MenuGroupDto>> getMenuById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getMenuById(id),
                "Menu details retrieved successfully"
        ));
    }

    @PostMapping("/menus")
    public ResponseEntity<ApiResponse<CmsMenuDto.MenuGroupDto>> createMenuGroup(@Valid @RequestBody CmsMenuDto.MenuGroupDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.createMenuGroup(dto),
                "Menu created successfully"
        ));
    }

    @PutMapping("/menus/{id}")
    public ResponseEntity<ApiResponse<CmsMenuDto.MenuGroupDto>> updateMenuGroup(
            @PathVariable Long id,
            @Valid @RequestBody CmsMenuDto.MenuGroupDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.updateMenuGroup(id, dto),
                "Menu updated successfully"
        ));
    }

    @DeleteMapping("/menus/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuGroup(@PathVariable Long id) {
        cmsService.deleteMenuGroup(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Menu deleted successfully"));
    }

    @PostMapping("/menus/{menuId}/items")
    public ResponseEntity<ApiResponse<CmsMenuDto.MenuItemDto>> addMenuItem(
            @PathVariable Long menuId,
            @Valid @RequestBody CmsMenuDto.MenuItemDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.addMenuItem(menuId, dto),
                "Menu item added successfully"
        ));
    }

    @PutMapping("/menus/items/{itemId}")
    public ResponseEntity<ApiResponse<CmsMenuDto.MenuItemDto>> updateMenuItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CmsMenuDto.MenuItemDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.updateMenuItem(itemId, dto),
                "Menu item updated successfully"
        ));
    }

    @DeleteMapping("/menus/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long itemId) {
        cmsService.deleteMenuItem(itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "Menu item deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 6. BANNERS
    // ─────────────────────────────────────────────────────────────────────────────

    @GetMapping("/banners")
    public ResponseEntity<ApiResponse<List<CmsBannerDto>>> getAllBanners(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String fileType) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getAllBanners(schoolId, search, fileType),
                "Banners retrieved successfully"
        ));
    }

    @PostMapping("/banners")
    public ResponseEntity<ApiResponse<CmsBannerDto>> createBanner(@Valid @RequestBody CmsBannerDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.createBanner(dto),
                "Banner created successfully"
        ));
    }

    @DeleteMapping("/banners/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        cmsService.deleteBanner(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Banner deleted successfully"));
    }

    @PostMapping("/banners/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> deleteBannersBulk(@RequestBody BulkDeleteRequest request) {
        cmsService.deleteBannersBulk(request.getIds());
        return ResponseEntity.ok(ApiResponse.success(null, "Banners deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 7. MEDIA
    // ─────────────────────────────────────────────────────────────────────────────

    @GetMapping("/media")
    public ResponseEntity<ApiResponse<List<CmsMediaDto>>> getAllMedia(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String fileType) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getAllMedia(schoolId, search, fileType),
                "Media items retrieved successfully"
        ));
    }

    @PostMapping("/media")
    public ResponseEntity<ApiResponse<CmsMediaDto>> createMedia(@Valid @RequestBody CmsMediaDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.createMedia(dto),
                "Media item created successfully"
        ));
    }

    @DeleteMapping("/media/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(@PathVariable Long id) {
        cmsService.deleteMedia(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Media item deleted successfully"));
    }

    @PostMapping("/media/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> deleteMediaBulk(@RequestBody BulkDeleteRequest request) {
        cmsService.deleteMediaBulk(request.getIds());
        return ResponseEntity.ok(ApiResponse.success(null, "Media items deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 8. TESTIMONIALS
    // ─────────────────────────────────────────────────────────────────────────────

    @GetMapping("/testimonials")
    public ResponseEntity<ApiResponse<List<CmsTestimonialDto>>> getAllTestimonials(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getAllTestimonials(schoolId, search),
                "Testimonials retrieved successfully"
        ));
    }

    @GetMapping("/testimonials/{id}")
    public ResponseEntity<ApiResponse<CmsTestimonialDto>> getTestimonialById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getTestimonialById(id),
                "Testimonial details retrieved successfully"
        ));
    }

    @PostMapping("/testimonials")
    public ResponseEntity<ApiResponse<CmsTestimonialDto>> createTestimonial(@Valid @RequestBody CmsTestimonialDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.createTestimonial(dto),
                "Testimonial created successfully"
        ));
    }

    @PutMapping("/testimonials/{id}")
    public ResponseEntity<ApiResponse<CmsTestimonialDto>> updateTestimonial(
            @PathVariable Long id,
            @Valid @RequestBody CmsTestimonialDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.updateTestimonial(id, dto),
                "Testimonial updated successfully"
        ));
    }

    @DeleteMapping("/testimonials/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTestimonial(@PathVariable Long id) {
        cmsService.deleteTestimonial(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Testimonial deleted successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 9. FRONT CMS SETTINGS
    // ─────────────────────────────────────────────────────────────────────────────

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<CmsSettingDto>> getSettings(
            @RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.getSettings(schoolId),
                "CMS Settings retrieved successfully"
        ));
    }

    @PostMapping("/settings")
    public ResponseEntity<ApiResponse<CmsSettingDto>> updateSettings(
            @RequestParam(required = false) Long schoolId,
            @RequestBody CmsSettingDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.updateSettings(schoolId, dto),
                "CMS Settings updated successfully"
        ));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<CmsSettingDto>> updateSettingsPut(
            @RequestParam(required = false) Long schoolId,
            @RequestBody CmsSettingDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                cmsService.updateSettings(schoolId, dto),
                "CMS Settings updated successfully"
        ));
    }
}

