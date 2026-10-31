package com.school.erp.service.cms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.cms.*;
import com.school.erp.entity.cms.*;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.cms.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsService {

    private final CmsEventRepository eventRepository;
    private final CmsGalleryRepository galleryRepository;
    private final CmsNoticeRepository noticeRepository;
    private final CmsPageRepository pageRepository;
    private final CmsMenuGroupRepository menuGroupRepository;
    private final CmsMenuItemRepository menuItemRepository;
    private final CmsBannerRepository bannerRepository;
    private final CmsMediaRepository mediaRepository;
    private final CmsTestimonialRepository testimonialRepository;
    private final CmsSettingRepository settingRepository;
    private final ObjectMapper objectMapper;

    // ─────────────────────────────────────────────────────────────────────────────
    // 1. EVENTS
    // ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CmsEventDto> getAllEvents(Long schoolId, String search) {
        return eventRepository.searchEvents(schoolId, search).stream()
                .map(this::toEventDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CmsEventDto getEventById(Long id) {
        CmsEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return toEventDto(event);
    }

    @Transactional
    public CmsEventDto createEvent(CmsEventDto dto) {
        CmsEvent event = CmsEvent.builder()
                .schoolId(dto.getSchoolId())
                .title(dto.getTitle())
                .venue(dto.getVenue())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .publishDate(dto.getPublishDate())
                .featuredImage(dto.getFeaturedImage())
                .description(dto.getDescription())
                .seoTitle(dto.getSeoTitle())
                .seoKeyword(dto.getSeoKeyword())
                .seoDescription(dto.getSeoDescription())
                .sidebar(dto.getSidebar() != null ? dto.getSidebar() : false)
                .build();
        return toEventDto(eventRepository.save(event));
    }

    @Transactional
    public CmsEventDto updateEvent(Long id, CmsEventDto dto) {
        CmsEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        event.setTitle(dto.getTitle());
        event.setVenue(dto.getVenue());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setPublishDate(dto.getPublishDate());
        if (dto.getFeaturedImage() != null) event.setFeaturedImage(dto.getFeaturedImage());
        event.setDescription(dto.getDescription());
        event.setSeoTitle(dto.getSeoTitle());
        event.setSeoKeyword(dto.getSeoKeyword());
        event.setSeoDescription(dto.getSeoDescription());
        if (dto.getSidebar() != null) event.setSidebar(dto.getSidebar());

        return toEventDto(eventRepository.save(event));
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    private CmsEventDto toEventDto(CmsEvent entity) {
        return CmsEventDto.builder()
                .id(entity.getId())
                .schoolId(entity.getSchoolId())
                .title(entity.getTitle())
                .venue(entity.getVenue())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .publishDate(entity.getPublishDate())
                .featuredImage(entity.getFeaturedImage())
                .description(entity.getDescription())
                .seoTitle(entity.getSeoTitle())
                .seoKeyword(entity.getSeoKeyword())
                .seoDescription(entity.getSeoDescription())
                .sidebar(entity.getSidebar())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2. GALLERIES
    // ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CmsGalleryDto> getAllGalleries(Long schoolId, String search) {
        return galleryRepository.searchGalleries(schoolId, search).stream()
                .map(this::toGalleryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CmsGalleryDto getGalleryById(Long id) {
        CmsGallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery not found with id: " + id));
        return toGalleryDto(gallery);
    }

    @Transactional
    public CmsGalleryDto createGallery(CmsGalleryDto dto) {
        String imagesJson = serializeImages(dto.getGalleryImages());
        String url = dto.getUrl();
        if (url == null || url.trim().isEmpty()) {
            url = "https://erp.schooly.com/gallery/" + slugify(dto.getTitle());
        }

        CmsGallery gallery = CmsGallery.builder()
                .schoolId(dto.getSchoolId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .featuredImage(dto.getFeaturedImage())
                .galleryImages(imagesJson)
                .url(url)
                .isInternalOnly(dto.getIsInternalOnly() != null ? dto.getIsInternalOnly() : false)
                .seoTitle(dto.getSeoTitle())
                .seoKeyword(dto.getSeoKeyword())
                .seoDescription(dto.getSeoDescription())
                .sidebar(dto.getSidebar() != null ? dto.getSidebar() : false)
                .build();
        return toGalleryDto(galleryRepository.save(gallery));
    }

    @Transactional
    public CmsGalleryDto updateGallery(Long id, CmsGalleryDto dto) {
        CmsGallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery not found with id: " + id));

        gallery.setTitle(dto.getTitle());
        gallery.setDescription(dto.getDescription());
        if (dto.getFeaturedImage() != null) gallery.setFeaturedImage(dto.getFeaturedImage());
        if (dto.getGalleryImages() != null) gallery.setGalleryImages(serializeImages(dto.getGalleryImages()));
        if (dto.getUrl() != null) gallery.setUrl(dto.getUrl());
        if (dto.getIsInternalOnly() != null) gallery.setIsInternalOnly(dto.getIsInternalOnly());
        gallery.setSeoTitle(dto.getSeoTitle());
        gallery.setSeoKeyword(dto.getSeoKeyword());
        gallery.setSeoDescription(dto.getSeoDescription());
        if (dto.getSidebar() != null) gallery.setSidebar(dto.getSidebar());

        return toGalleryDto(galleryRepository.save(gallery));
    }

    @Transactional
    public void deleteGallery(Long id) {
        if (!galleryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Gallery not found with id: " + id);
        }
        galleryRepository.deleteById(id);
    }

    private CmsGalleryDto toGalleryDto(CmsGallery entity) {
        return CmsGalleryDto.builder()
                .id(entity.getId())
                .schoolId(entity.getSchoolId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .featuredImage(entity.getFeaturedImage())
                .galleryImages(deserializeImages(entity.getGalleryImages()))
                .url(entity.getUrl())
                .isInternalOnly(entity.getIsInternalOnly())
                .seoTitle(entity.getSeoTitle())
                .seoKeyword(entity.getSeoKeyword())
                .seoDescription(entity.getSeoDescription())
                .sidebar(entity.getSidebar())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3. NOTICES
    // ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CmsNoticeDto> getAllNotices(Long schoolId, String search) {
        return noticeRepository.searchNotices(schoolId, search).stream()
                .map(this::toNoticeDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CmsNoticeDto getNoticeById(Long id) {
        CmsNotice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + id));
        return toNoticeDto(notice);
    }

    @Transactional
    public CmsNoticeDto createNotice(CmsNoticeDto dto) {
        String url = dto.getUrl();
        if (url == null || url.trim().isEmpty()) {
            url = "https://erp.schooly.com/news/" + slugify(dto.getTitle());
        }

        CmsNotice notice = CmsNotice.builder()
                .schoolId(dto.getSchoolId())
                .title(dto.getTitle())
                .date(dto.getDate())
                .featuredImage(dto.getFeaturedImage())
                .description(dto.getDescription())
                .url(url)
                .seoTitle(dto.getSeoTitle())
                .seoKeyword(dto.getSeoKeyword())
                .seoDescription(dto.getSeoDescription())
                .sidebar(dto.getSidebar() != null ? dto.getSidebar() : false)
                .build();
        return toNoticeDto(noticeRepository.save(notice));
    }

    @Transactional
    public CmsNoticeDto updateNotice(Long id, CmsNoticeDto dto) {
        CmsNotice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + id));

        notice.setTitle(dto.getTitle());
        notice.setDate(dto.getDate());
        if (dto.getFeaturedImage() != null) notice.setFeaturedImage(dto.getFeaturedImage());
        notice.setDescription(dto.getDescription());
        if (dto.getUrl() != null) notice.setUrl(dto.getUrl());
        notice.setSeoTitle(dto.getSeoTitle());
        notice.setSeoKeyword(dto.getSeoKeyword());
        notice.setSeoDescription(dto.getSeoDescription());
        if (dto.getSidebar() != null) notice.setSidebar(dto.getSidebar());

        return toNoticeDto(noticeRepository.save(notice));
    }

    @Transactional
    public void deleteNotice(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notice not found with id: " + id);
        }
        noticeRepository.deleteById(id);
    }

    private CmsNoticeDto toNoticeDto(CmsNotice entity) {
        return CmsNoticeDto.builder()
                .id(entity.getId())
                .schoolId(entity.getSchoolId())
                .title(entity.getTitle())
                .date(entity.getDate())
                .featuredImage(entity.getFeaturedImage())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .seoTitle(entity.getSeoTitle())
                .seoKeyword(entity.getSeoKeyword())
                .seoDescription(entity.getSeoDescription())
                .sidebar(entity.getSidebar())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4. PAGES
    // ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CmsPageDto> getAllPages(Long schoolId, String search) {
        List<CmsPage> pages;
        if (search == null || search.trim().isEmpty()) {
            if (schoolId == null) {
                pages = pageRepository.findAllByOrderByIdDesc();
            } else {
                pages = pageRepository.findBySchoolIdOrderByIdDesc(schoolId);
            }
        } else {
            pages = pageRepository.searchPages(schoolId, search.trim());
        }
        return pages.stream()
                .map(this::toPageDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CmsPageDto getPageById(Long id) {
        CmsPage page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + id));
        return toPageDto(page);
    }

    @Transactional
    public CmsPageDto createPage(CmsPageDto dto) {
        String url = dto.getUrl();
        if (url == null || url.trim().isEmpty()) {
            url = "https://erp.schooly.com/page/" + slugify(dto.getTitle());
        }

        CmsPage page = CmsPage.builder()
                .schoolId(dto.getSchoolId())
                .title(dto.getTitle())
                .pageType(dto.getPageType() != null ? dto.getPageType() : "Standard")
                .url(url)
                .featuredImage(dto.getFeaturedImage())
                .description(dto.getDescription())
                .seoTitle(dto.getSeoTitle())
                .seoKeyword(dto.getSeoKeyword())
                .seoDescription(dto.getSeoDescription())
                .sidebar(dto.getSidebar() != null ? dto.getSidebar() : false)
                .build();
        return toPageDto(pageRepository.save(page));
    }

    @Transactional
    public CmsPageDto updatePage(Long id, CmsPageDto dto) {
        CmsPage page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + id));

        page.setTitle(dto.getTitle());
        if (dto.getPageType() != null) page.setPageType(dto.getPageType());
        if (dto.getUrl() != null) page.setUrl(dto.getUrl());
        if (dto.getFeaturedImage() != null) page.setFeaturedImage(dto.getFeaturedImage());
        page.setDescription(dto.getDescription());
        page.setSeoTitle(dto.getSeoTitle());
        page.setSeoKeyword(dto.getSeoKeyword());
        page.setSeoDescription(dto.getSeoDescription());
        if (dto.getSidebar() != null) page.setSidebar(dto.getSidebar());

        return toPageDto(pageRepository.save(page));
    }

    @Transactional
    public void deletePage(Long id) {
        if (!pageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Page not found with id: " + id);
        }
        pageRepository.deleteById(id);
    }

    private CmsPageDto toPageDto(CmsPage entity) {
        return CmsPageDto.builder()
                .id(entity.getId())
                .schoolId(entity.getSchoolId())
                .title(entity.getTitle())
                .pageType(entity.getPageType())
                .url(entity.getUrl())
                .featuredImage(entity.getFeaturedImage())
                .description(entity.getDescription())
                .seoTitle(entity.getSeoTitle())
                .seoKeyword(entity.getSeoKeyword())
                .seoDescription(entity.getSeoDescription())
                .sidebar(entity.getSidebar())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 5. MENUS
    // ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CmsMenuDto.MenuGroupDto> getAllMenus(Long schoolId, String search) {
        List<CmsMenuGroup> groups = menuGroupRepository.searchMenuGroups(schoolId, search);
        return groups.stream().map(this::toMenuGroupDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CmsMenuDto.MenuGroupDto getMenuById(Long id) {
        CmsMenuGroup group = menuGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with id: " + id));
        return toMenuGroupDto(group);
    }

    @Transactional
    public CmsMenuDto.MenuGroupDto createMenuGroup(CmsMenuDto.MenuGroupDto dto) {
        CmsMenuGroup group = CmsMenuGroup.builder()
                .schoolId(dto.getSchoolId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .openInNewTab(dto.getOpenInNewTab() != null ? dto.getOpenInNewTab() : false)
                .build();
        CmsMenuGroup saved = menuGroupRepository.save(group);
        return toMenuGroupDto(saved);
    }

    @Transactional
    public CmsMenuDto.MenuGroupDto updateMenuGroup(Long id, CmsMenuDto.MenuGroupDto dto) {
        CmsMenuGroup group = menuGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with id: " + id));
        group.setTitle(dto.getTitle());
        group.setDescription(dto.getDescription());
        if (dto.getOpenInNewTab() != null) group.setOpenInNewTab(dto.getOpenInNewTab());
        return toMenuGroupDto(menuGroupRepository.save(group));
    }

    @Transactional
    public void deleteMenuGroup(Long id) {
        if (!menuGroupRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu not found with id: " + id);
        }
        menuItemRepository.deleteByMenuGroupId(id);
        menuGroupRepository.deleteById(id);
    }

    @Transactional
    public CmsMenuDto.MenuItemDto addMenuItem(Long menuGroupId, CmsMenuDto.MenuItemDto dto) {
        if (!menuGroupRepository.existsById(menuGroupId)) {
            throw new ResourceNotFoundException("Menu group not found with id: " + menuGroupId);
        }
        CmsMenuItem item = CmsMenuItem.builder()
                .menuGroupId(menuGroupId)
                .title(dto.getTitle())
                .menuType(dto.getMenuType() != null ? dto.getMenuType() : "external")
                .url(dto.getUrl())
                .pageId(dto.getPageId())
                .openInNewTab(dto.getOpenInNewTab() != null ? dto.getOpenInNewTab() : false)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .parentId(dto.getParentId())
                .build();
        return toMenuItemDto(menuItemRepository.save(item));
    }

    @Transactional
    public CmsMenuDto.MenuItemDto updateMenuItem(Long itemId, CmsMenuDto.MenuItemDto dto) {
        CmsMenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + itemId));
        item.setTitle(dto.getTitle());
        if (dto.getMenuType() != null) item.setMenuType(dto.getMenuType());
        item.setUrl(dto.getUrl());
        item.setPageId(dto.getPageId());
        if (dto.getOpenInNewTab() != null) item.setOpenInNewTab(dto.getOpenInNewTab());
        if (dto.getSortOrder() != null) item.setSortOrder(dto.getSortOrder());
        item.setParentId(dto.getParentId());
        return toMenuItemDto(menuItemRepository.save(item));
    }

    @Transactional
    public void deleteMenuItem(Long itemId) {
        if (!menuItemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Menu item not found with id: " + itemId);
        }
        menuItemRepository.deleteById(itemId);
    }

    private CmsMenuDto.MenuGroupDto toMenuGroupDto(CmsMenuGroup group) {
        List<CmsMenuItem> allItems = menuItemRepository.findByMenuGroupIdOrderBySortOrderAscIdAsc(group.getId());
        List<CmsMenuDto.MenuItemDto> dtoList = allItems.stream().map(this::toMenuItemDto).collect(Collectors.toList());

        // Build hierarchical tree
        Map<Long, CmsMenuDto.MenuItemDto> lookup = new HashMap<>();
        for (CmsMenuDto.MenuItemDto item : dtoList) {
            lookup.put(item.getId(), item);
        }
        List<CmsMenuDto.MenuItemDto> rootItems = new ArrayList<>();
        for (CmsMenuDto.MenuItemDto item : dtoList) {
            if (item.getParentId() != null && lookup.containsKey(item.getParentId())) {
                lookup.get(item.getParentId()).getChildren().add(item);
            } else {
                rootItems.add(item);
            }
        }

        return CmsMenuDto.MenuGroupDto.builder()
                .id(group.getId())
                .schoolId(group.getSchoolId())
                .title(group.getTitle())
                .description(group.getDescription())
                .openInNewTab(group.getOpenInNewTab())
                .items(rootItems)
                .build();
    }

    private CmsMenuDto.MenuItemDto toMenuItemDto(CmsMenuItem item) {
        return CmsMenuDto.MenuItemDto.builder()
                .id(item.getId())
                .menuGroupId(item.getMenuGroupId())
                .title(item.getTitle())
                .menuType(item.getMenuType())
                .url(item.getUrl())
                .pageId(item.getPageId())
                .openInNewTab(item.getOpenInNewTab())
                .sortOrder(item.getSortOrder())
                .parentId(item.getParentId())
                .children(new ArrayList<>())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 6. BANNERS
    // ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CmsBannerDto> getAllBanners(Long schoolId, String search, String fileType) {
        return bannerRepository.searchBanners(schoolId, search, fileType).stream()
                .map(this::toBannerDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CmsBannerDto createBanner(CmsBannerDto dto) {
        CmsBanner banner = CmsBanner.builder()
                .schoolId(dto.getSchoolId())
                .fileName(dto.getFileName())
                .url(dto.getUrl())
                .fileType(dto.getFileType() != null ? dto.getFileType() : "image")
                .dimensions(dto.getDimensions() != null ? dto.getDimensions() : "1920 x 800")
                .size(dto.getSize() != null ? dto.getSize() : "1.2 MB")
                .uploadDate(dto.getUploadDate() != null ? dto.getUploadDate() : "Today")
                .build();
        return toBannerDto(bannerRepository.save(banner));
    }

    @Transactional
    public void deleteBanner(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Banner not found with id: " + id);
        }
        bannerRepository.deleteById(id);
    }

    @Transactional
    public void deleteBannersBulk(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            bannerRepository.deleteAllById(ids);
        }
    }

    private CmsBannerDto toBannerDto(CmsBanner entity) {
        return CmsBannerDto.builder()
                .id(entity.getId())
                .schoolId(entity.getSchoolId())
                .fileName(entity.getFileName())
                .url(entity.getUrl())
                .fileType(entity.getFileType())
                .dimensions(entity.getDimensions())
                .size(entity.getSize())
                .uploadDate(entity.getUploadDate())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 7. MEDIA
    // ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CmsMediaDto> getAllMedia(Long schoolId, String search, String fileType) {
        return mediaRepository.searchMedia(schoolId, search, fileType).stream()
                .map(this::toMediaDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CmsMediaDto createMedia(CmsMediaDto dto) {
        CmsMedia media = CmsMedia.builder()
                .schoolId(dto.getSchoolId())
                .fileName(dto.getFileName())
                .fileType(dto.getFileType() != null ? dto.getFileType() : "image")
                .url(dto.getUrl())
                .size(dto.getSize() != null ? dto.getSize() : "1.0 MB")
                .dimensions(dto.getDimensions())
                .uploadDate(dto.getUploadDate() != null ? dto.getUploadDate() : "Today")
                .youtubeUrl(dto.getYoutubeUrl())
                .build();
        return toMediaDto(mediaRepository.save(media));
    }

    @Transactional
    public void deleteMedia(Long id) {
        if (!mediaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Media not found with id: " + id);
        }
        mediaRepository.deleteById(id);
    }

    @Transactional
    public void deleteMediaBulk(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            mediaRepository.deleteAllById(ids);
        }
    }

    private CmsMediaDto toMediaDto(CmsMedia entity) {
        return CmsMediaDto.builder()
                .id(entity.getId())
                .schoolId(entity.getSchoolId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .url(entity.getUrl())
                .size(entity.getSize())
                .dimensions(entity.getDimensions())
                .uploadDate(entity.getUploadDate())
                .youtubeUrl(entity.getYoutubeUrl())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 8. TESTIMONIALS
    // ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CmsTestimonialDto> getAllTestimonials(Long schoolId, String search) {
        return testimonialRepository.searchTestimonials(schoolId, search).stream()
                .map(this::toTestimonialDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CmsTestimonialDto getTestimonialById(Long id) {
        CmsTestimonial item = testimonialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found with id: " + id));
        return toTestimonialDto(item);
    }

    @Transactional
    public CmsTestimonialDto createTestimonial(CmsTestimonialDto dto) {
        CmsTestimonial item = CmsTestimonial.builder()
                .schoolId(dto.getSchoolId())
                .name(dto.getName())
                .designation(dto.getDesignation())
                .testimonial(dto.getTestimonial())
                .fullTestimonial(dto.getFullTestimonial() != null ? dto.getFullTestimonial() : dto.getTestimonial())
                .image(dto.getImage())
                .rating(dto.getRating() != null ? dto.getRating() : 5)
                .date(dto.getDate() != null ? dto.getDate() : "2026-10-30")
                .build();
        return toTestimonialDto(testimonialRepository.save(item));
    }

    @Transactional
    public CmsTestimonialDto updateTestimonial(Long id, CmsTestimonialDto dto) {
        CmsTestimonial item = testimonialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonial not found with id: " + id));

        item.setName(dto.getName());
        item.setDesignation(dto.getDesignation());
        item.setTestimonial(dto.getTestimonial());
        item.setFullTestimonial(dto.getFullTestimonial() != null ? dto.getFullTestimonial() : dto.getTestimonial());
        if (dto.getImage() != null) item.setImage(dto.getImage());
        if (dto.getRating() != null) item.setRating(dto.getRating());
        if (dto.getDate() != null) item.setDate(dto.getDate());

        return toTestimonialDto(testimonialRepository.save(item));
    }

    @Transactional
    public void deleteTestimonial(Long id) {
        if (!testimonialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Testimonial not found with id: " + id);
        }
        testimonialRepository.deleteById(id);
    }

    private CmsTestimonialDto toTestimonialDto(CmsTestimonial entity) {
        return CmsTestimonialDto.builder()
                .id(entity.getId())
                .schoolId(entity.getSchoolId())
                .name(entity.getName())
                .designation(entity.getDesignation())
                .testimonial(entity.getTestimonial())
                .fullTestimonial(entity.getFullTestimonial())
                .image(entity.getImage())
                .rating(entity.getRating())
                .date(entity.getDate())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // HELPERS & INITIAL DATA SEEDER
    // ─────────────────────────────────────────────────────────────────────────────

    private String serializeImages(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> deserializeImages(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String slugify(String text) {
        if (text == null) return "item";
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initSampleData() {
        try {
            seedTestimonials();
            seedEvents();
            seedGalleries();
            seedNotices();
            seedPages();
            seedMenus();
            seedBanners();
            seedMedia();
            seedSettings();
            log.info("CMS sample data verified and initialized successfully.");
        } catch (Exception e) {
            log.warn("CMS sample data initialization skipped or partially completed: {}", e.getMessage());
        }
    }

    private void seedTestimonials() {
        if (testimonialRepository.count() > 0) return;
        testimonialRepository.save(CmsTestimonial.builder()
                .name("Mr. XYZ")
                .testimonial("Testing the testimonial nmb")
                .fullTestimonial("Testing the testimonial nmb. The school delivers high-quality education and student guidance.")
                .image("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80")
                .rating(5)
                .designation("Guardian")
                .date("2026-10-24")
                .build());
        testimonialRepository.save(CmsTestimonial.builder()
                .name("himanshu")
                .testimonial("Cambridge specializes in educating children with dyslexia, ADHD, and other learning disabilities. Re...")
                .fullTestimonial("Cambridge specializes in educating children with dyslexia, ADHD, and other learning disabilities. Resource rooms and personalized learning strategies ensure every student succeeds.")
                .image("https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80")
                .rating(5)
                .designation("Alumni / Parent")
                .date("2026-10-18")
                .build());
        testimonialRepository.save(CmsTestimonial.builder()
                .name("Nirmala")
                .testimonial("We had a wonderful experience with the school, the management listens all the problems and solve it...")
                .fullTestimonial("We had a wonderful experience with the school, the management listens all the problems and solve it immediately. Very supportive staff and excellent academics.")
                .image("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80")
                .rating(5)
                .designation("Parent")
                .date("2026-10-12")
                .build());
    }

    private void seedEvents() {
        if (eventRepository.count() > 0) return;
        eventRepository.save(CmsEvent.builder()
                .title("Special Exhibition")
                .venue("Campus Main Auditorium")
                .startDate("10/24/2026")
                .endDate("10/28/2026")
                .publishDate("10/20/2026")
                .description("Annual science and art exhibition featuring creative student projects, interactive booths, and community showcases.")
                .seoTitle("Special Exhibition 2026")
                .seoKeyword("school, exhibition, student projects")
                .seoDescription("Annual student innovation and art exhibition.")
                .build());
        eventRepository.save(CmsEvent.builder()
                .title("Annual Sports Meet 2026")
                .venue("Olympic Athletics Stadium")
                .startDate("11/15/2026")
                .endDate("11/17/2026")
                .publishDate("11/01/2026")
                .description("Three-day sports festival uniting students across track, field, indoor games, and championship awards.")
                .build());
    }

    private void seedGalleries() {
        if (galleryRepository.count() > 0) return;
        galleryRepository.save(CmsGallery.builder()
                .title("Special Exhibition")
                .description("Memorable snapshots from the annual campus science and art exhibition.")
                .url("https://erp.schooly.com/gallery/special-exhibition")
                .isInternalOnly(false)
                .build());
        galleryRepository.save(CmsGallery.builder()
                .title("Campus Life")
                .description("Daily student activities, outdoor learning sessions, and laboratory experiments.")
                .url("https://erp.schooly.com/gallery/campus-life")
                .isInternalOnly(true)
                .build());
    }

    private void seedNotices() {
        if (noticeRepository.count() > 0) return;
        noticeRepository.save(CmsNotice.builder()
                .title("12 Things Successful Mompreneurs Do Differently")
                .date("10/20/2026")
                .description("<p>Insights and life lessons from modern community leaders and entrepreneurial parents.</p>")
                .url("https://erp.schooly.com/news/12-things-successful-mompreneurs")
                .build());
        noticeRepository.save(CmsNotice.builder()
                .title("New Semester Curriculum & Timetable Announcement")
                .date("10/15/2026")
                .description("<p>The updated syllabus, laboratory schedules, and faculty assignments are now published.</p>")
                .url("https://erp.schooly.com/news/semester-curriculum-announcement")
                .build());
    }

    private void seedPages() {
        seedPageIfMissing("About Us", "Standard", "https://erp.schooly.com/pages/about-us",
                "<p>Empowering minds and inspiring generations with world-class educational experiences, leadership values, and research-led holistic instruction.</p>",
                "https://images.unsplash.com/photo-1543269865-cbf427effbad?w=800&auto=format&fit=crop&q=80");
        seedPageIfMissing("Philosophy", "Standard", "https://erp.schooly.com/pages/philosophy",
                "<p>Our core pedagogical philosophy, student-centric learning ethos, and values-driven holistic curriculum that nurtures global citizens.</p>",
                "https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=800&auto=format&fit=crop&q=80");
        seedPageIfMissing("Principal Message", "Standard", "https://erp.schooly.com/pages/principal-message",
                "<p>Term circular from the Principal outlining co-curricular activities, exam preparedness, scientific rigor, and mentor guidance.</p>",
                "https://images.unsplash.com/photo-1577495508048-b635879837f1?w=800&auto=format&fit=crop&q=80");
        seedPageIfMissing("Manager Message", "Standard", "https://erp.schooly.com/pages/manager-message",
                "<p>Institutional administrative manager welcome note, operational highlights, state-of-the-art campus infrastructure, and community updates.</p>",
                null);
        seedPageIfMissing("Principal's Message", "Standard", "https://erp.schooly.com/pages/principals-message",
                "<p>Annual welcome letter from the Principal, inspiring academic excellence, integrity, character development, and lifelong learning.</p>",
                null);
        seedPageIfMissing("CEO Message", "Standard", "https://erp.schooly.com/pages/ceo-message-t",
                "<p>Address from the Chief Executive Officer outlining leadership principles, continuous investment in STEM, and the vision for institutional growth.</p>",
                "https://images.unsplash.com/photo-1543269865-cbf427effbad?w=800&auto=format&fit=crop&q=80");
        seedPageIfMissing("Admissions Policy", "Standard", "https://erp.schooly.com/pages/admissions-policy",
                "<p>Comprehensive guide to applicant eligibility, requirements, scholarship entrance tests, reservation criteria, and enrollment procedures for the 2026-27 academic year.</p>",
                "https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=800&auto=format&fit=crop&q=80");
        seedPageIfMissing("Our Faculty", "Standard", "https://erp.schooly.com/pages/faculty-staff",
                "<p>Meet our distinguished educators, doctoral faculty, department chairs, and career counselors who provide continuous academic mentorship and pastoral care.</p>",
                "https://images.unsplash.com/photo-1577495508048-b635879837f1?w=800&auto=format&fit=crop&q=80");
        seedPageIfMissing("Careers & Recruitment", "Standard", "https://erp.schooly.com/pages/careers",
                "<p>Join our progressive educational team. Explore open faculty positions, teaching fellowships, competitive benefits packages, and career advancement opportunities.</p>",
                "https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=800&auto=format&fit=crop&q=80");
        seedPageIfMissing("Contact Us", "Standard", "https://erp.schooly.com/pages/contact-us",
                "<p>Connect with campus admissions, student counseling, infirmary services, and principal secretariats. Campus hours: Monday to Friday (8:00 AM – 3:30 PM).</p>",
                "https://images.unsplash.com/photo-1497366216548-37526070297c?w=800&auto=format&fit=crop&q=80");
        seedPageIfMissing("School Motto", "Standard", "https://erp.schooly.com/pages/school-motto",
                "<p>Knowledge, Integrity, and Excellence — our historic motto, shield heritage, and institutional crest history.</p>",
                null);
    }

    private void seedPageIfMissing(String title, String pageType, String url, String description, String featuredImage) {
        if (!pageRepository.existsByTitleIgnoreCase(title)) {
            pageRepository.save(CmsPage.builder()
                    .title(title)
                    .pageType(pageType)
                    .url(url)
                    .description(description)
                    .featuredImage(featuredImage)
                    .sidebar(true)
                    .build());
        }
    }

    private void seedMenus() {
        if (menuGroupRepository.count() > 0) return;
        CmsMenuGroup header = menuGroupRepository.save(CmsMenuGroup.builder()
                .title("Header")
                .description("Primary header navigation bar")
                .build());

        menuItemRepository.save(CmsMenuItem.builder()
                .menuGroupId(header.getId())
                .title("Home")
                .menuType("page")
                .url("/home")
                .sortOrder(1)
                .build());

        CmsMenuItem about = menuItemRepository.save(CmsMenuItem.builder()
                .menuGroupId(header.getId())
                .title("About Us")
                .menuType("page")
                .url("/about")
                .sortOrder(2)
                .build());

        menuItemRepository.save(CmsMenuItem.builder()
                .menuGroupId(header.getId())
                .title("Philosophy")
                .menuType("page")
                .url("/about/philosophy")
                .parentId(about.getId())
                .sortOrder(1)
                .build());

        menuItemRepository.save(CmsMenuItem.builder()
                .menuGroupId(header.getId())
                .title("Principal Message")
                .menuType("page")
                .url("/about/principal-message")
                .parentId(about.getId())
                .sortOrder(2)
                .build());
    }

    private void seedBanners() {
        if (bannerRepository.count() > 0) return;
        bannerRepository.save(CmsBanner.builder()
                .fileName("Experience Zora Global School | Salem | Campus Tour")
                .url("https://images.unsplash.com/photo-1541829070764-84a7d30dd3f3?w=1200&auto=format&fit=crop&q=80")
                .fileType("image")
                .dimensions("1920 x 800")
                .size("2.4 MB")
                .uploadDate("2026-10-27")
                .build());
        bannerRepository.save(CmsBanner.builder()
                .fileName("Transform Your School Into a Smart Campus")
                .url("https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=1200&auto=format&fit=crop&q=80")
                .fileType("image")
                .dimensions("1920 x 800")
                .size("1.8 MB")
                .uploadDate("2026-10-26")
                .build());
    }

    private void seedMedia() {
        if (mediaRepository.count() > 0) return;
        mediaRepository.save(CmsMedia.builder()
                .fileName("Campus Tour High Res.jpg")
                .fileType("image")
                .url("https://images.unsplash.com/photo-1562774053-701939374585?w=800&auto=format&fit=crop&q=80")
                .size("3.2 MB")
                .dimensions("2560 x 1440")
                .uploadDate("2026-10-25")
                .build());
        mediaRepository.save(CmsMedia.builder()
                .fileName("School Prospectus 2026.pdf")
                .fileType("pdf")
                .url("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")
                .size("1.1 MB")
                .uploadDate("2026-10-24")
                .build());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 9. SETTINGS
    // ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CmsSettingDto getSettings(Long schoolId) {
        Optional<CmsSetting> opt = (schoolId != null)
                ? settingRepository.findFirstBySchoolIdOrderByIdAsc(schoolId)
                : settingRepository.findFirstByOrderByIdAsc();

        if (opt.isEmpty()) {
            opt = settingRepository.findFirstByOrderByIdAsc();
        }

        return opt.map(this::toSettingDto).orElseGet(() -> getDefaultSettingDto(schoolId));
    }

    @Transactional
    public CmsSettingDto updateSettings(Long schoolId, CmsSettingDto dto) {
        Long targetSchoolId = (dto.getSchoolId() != null) ? dto.getSchoolId() : schoolId;
        Optional<CmsSetting> opt = (targetSchoolId != null)
                ? settingRepository.findFirstBySchoolIdOrderByIdAsc(targetSchoolId)
                : settingRepository.findFirstByOrderByIdAsc();

        CmsSetting setting = opt.orElseGet(() -> CmsSetting.builder().schoolId(targetSchoolId).build());

        if (dto.getWebsiteLogo() != null) setting.setWebsiteLogo(dto.getWebsiteLogo());
        if (dto.getFavIcon() != null) setting.setFavIcon(dto.getFavIcon());
        if (dto.getContactEmail() != null) setting.setContactEmail(dto.getContactEmail());
        if (dto.getComplainEmail() != null) setting.setComplainEmail(dto.getComplainEmail());
        if (dto.getFooterText() != null) setting.setFooterText(dto.getFooterText());
        if (dto.getSchoolAppEnabled() != null) setting.setSchoolAppEnabled(dto.getSchoolAppEnabled());
        if (dto.getFrontWebsiteEnabled() != null) setting.setFrontWebsiteEnabled(dto.getFrontWebsiteEnabled());
        if (dto.getSchoolTourVideoUrl() != null) setting.setSchoolTourVideoUrl(dto.getSchoolTourVideoUrl());
        if (dto.getAppDownloadLink() != null) setting.setAppDownloadLink(dto.getAppDownloadLink());
        if (dto.getIosAppLink() != null) setting.setIosAppLink(dto.getIosAppLink());
        if (dto.getThemePrimaryColor() != null) setting.setThemePrimaryColor(dto.getThemePrimaryColor());
        if (dto.getThemeStandardColor() != null) setting.setThemeStandardColor(dto.getThemeStandardColor());
        if (dto.getGoogleAnalytics() != null) setting.setGoogleAnalytics(dto.getGoogleAnalytics());
        if (dto.getWelcomePopupTitle() != null) setting.setWelcomePopupTitle(dto.getWelcomePopupTitle());
        if (dto.getWelcomePopupImage() != null) setting.setWelcomePopupImage(dto.getWelcomePopupImage());

        if (dto.getFacebookUrl() != null) setting.setFacebookUrl(dto.getFacebookUrl());
        if (dto.getInstagramUrl() != null) setting.setInstagramUrl(dto.getInstagramUrl());
        if (dto.getWhatsappUrl() != null) setting.setWhatsappUrl(dto.getWhatsappUrl());
        if (dto.getXUrl() != null) setting.setXUrl(dto.getXUrl());
        if (dto.getLinkedinUrl() != null) setting.setLinkedinUrl(dto.getLinkedinUrl());
        if (dto.getYoutubeUrl() != null) setting.setYoutubeUrl(dto.getYoutubeUrl());

        if (dto.getAboutTitle() != null) setting.setAboutTitle(dto.getAboutTitle());
        if (dto.getAboutDescription() != null) setting.setAboutDescription(dto.getAboutDescription());
        if (dto.getAboutImage() != null) setting.setAboutImage(dto.getAboutImage());

        if (dto.getMissionTitle() != null) setting.setMissionTitle(dto.getMissionTitle());
        if (dto.getMissionDescription() != null) setting.setMissionDescription(dto.getMissionDescription());
        if (dto.getMissionImage() != null) setting.setMissionImage(dto.getMissionImage());

        if (dto.getVisionTitle() != null) setting.setVisionTitle(dto.getVisionTitle());
        if (dto.getVisionDescription() != null) setting.setVisionDescription(dto.getVisionDescription());
        if (dto.getVisionImage() != null) setting.setVisionImage(dto.getVisionImage());

        if (dto.getMetaDescription() != null) setting.setMetaDescription(dto.getMetaDescription());
        if (dto.getMetaTitle() != null) setting.setMetaTitle(dto.getMetaTitle());
        if (dto.getMetaKeywords() != null) setting.setMetaKeywords(dto.getMetaKeywords());
        if (dto.getOgTitle() != null) setting.setOgTitle(dto.getOgTitle());
        if (dto.getOgDescription() != null) setting.setOgDescription(dto.getOgDescription());
        if (dto.getOgImageUrl() != null) setting.setOgImageUrl(dto.getOgImageUrl());
        if (dto.getOgUrl() != null) setting.setOgUrl(dto.getOgUrl());
        if (dto.getOgType() != null) setting.setOgType(dto.getOgType());

        if (dto.getActiveThemeId() != null) setting.setActiveThemeId(dto.getActiveThemeId());

        CmsSetting saved = settingRepository.save(setting);
        return toSettingDto(saved);
    }

    private void seedSettings() {
        if (settingRepository.count() > 0) return;
        CmsSetting setting = CmsSetting.builder()
                .websiteLogo("https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=300&auto=format&fit=crop&q=80")
                .favIcon("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=100&auto=format&fit=crop&q=80")
                .contactEmail("contact@nlet.in")
                .complainEmail("complaint@nlet.in")
                .footerText("Copyright © 2026 NLET School. All rights reserved.")
                .schoolAppEnabled(true)
                .frontWebsiteEnabled(true)
                .schoolTourVideoUrl("https://www.youtube.com/watch?v=1bnZy44h0B")
                .appDownloadLink("https://play.google.com/store/apps/details?id=com.nlet.school")
                .iosAppLink("https://apps.apple.com/app/nlet-school/id123456789")
                .themePrimaryColor("#1b2a47")
                .themeStandardColor("#dc2626")
                .googleAnalytics("<script async src=\"https://www.googletagmanager.com/gtag/js?id=G-NLET2026\"></script>")
                .welcomePopupTitle("Welcome to NLET School")
                .welcomePopupImage("https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=600&auto=format&fit=crop&q=80")
                .facebookUrl("https://facebook.com/nlet.school")
                .instagramUrl("https://instagram.com/nlet.school")
                .whatsappUrl("https://wa.me/919876543210")
                .xUrl("https://x.com/nlet_school")
                .linkedinUrl("https://linkedin.com/company/nlet")
                .youtubeUrl("https://youtube.com/@nletschool")
                .aboutTitle("Students Council")
                .aboutDescription("Students council plays a great role for students to take on leadership roles, promote the voice of the students, solve their problems and improve their community.")
                .aboutImage("https://images.unsplash.com/photo-1577896851231-70ef18881754?w=600&auto=format&fit=crop&q=80")
                .missionTitle("Academics")
                .missionDescription("Academics involves everything dividing from alphabets to thin lesson. Academic achievement is important for the successful development young people in society.")
                .missionImage("https://images.unsplash.com/photo-1524178232363-1fb2b075b655?w=600&auto=format&fit=crop&q=80")
                .visionTitle("Athletics")
                .visionDescription("Sports and physical health helps students to recognize their talent and abilities without exam.")
                .visionImage("https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=600&auto=format&fit=crop&q=80")
                .metaDescription("Leading K-12 institutional management and excellence in education, arts, athletics, and scientific innovation.")
                .metaTitle("NLET ERP Institute Management Software")
                .metaKeywords("School ERP, Institute Management, Admissions, Online Fees, Attendance, Examination Portal")
                .ogTitle("NLET Public School & Institute Portal")
                .ogDescription("Empowering generations with global educational standards and innovative campus technology.")
                .ogImageUrl("https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=1200")
                .ogUrl("https://erp.schooly.com")
                .ogType("website")
                .activeThemeId(1)
                .build();
        settingRepository.save(setting);
    }

    private CmsSettingDto toSettingDto(CmsSetting entity) {
        return CmsSettingDto.builder()
                .id(entity.getId())
                .schoolId(entity.getSchoolId())
                .websiteLogo(entity.getWebsiteLogo())
                .favIcon(entity.getFavIcon())
                .contactEmail(entity.getContactEmail())
                .complainEmail(entity.getComplainEmail())
                .footerText(entity.getFooterText())
                .schoolAppEnabled(entity.getSchoolAppEnabled())
                .frontWebsiteEnabled(entity.getFrontWebsiteEnabled())
                .schoolTourVideoUrl(entity.getSchoolTourVideoUrl())
                .appDownloadLink(entity.getAppDownloadLink())
                .iosAppLink(entity.getIosAppLink())
                .themePrimaryColor(entity.getThemePrimaryColor())
                .themeStandardColor(entity.getThemeStandardColor())
                .googleAnalytics(entity.getGoogleAnalytics())
                .welcomePopupTitle(entity.getWelcomePopupTitle())
                .welcomePopupImage(entity.getWelcomePopupImage())
                .facebookUrl(entity.getFacebookUrl())
                .instagramUrl(entity.getInstagramUrl())
                .whatsappUrl(entity.getWhatsappUrl())
                .xUrl(entity.getXUrl())
                .linkedinUrl(entity.getLinkedinUrl())
                .youtubeUrl(entity.getYoutubeUrl())
                .aboutTitle(entity.getAboutTitle())
                .aboutDescription(entity.getAboutDescription())
                .aboutImage(entity.getAboutImage())
                .missionTitle(entity.getMissionTitle())
                .missionDescription(entity.getMissionDescription())
                .missionImage(entity.getMissionImage())
                .visionTitle(entity.getVisionTitle())
                .visionDescription(entity.getVisionDescription())
                .visionImage(entity.getVisionImage())
                .metaDescription(entity.getMetaDescription())
                .metaTitle(entity.getMetaTitle())
                .metaKeywords(entity.getMetaKeywords())
                .ogTitle(entity.getOgTitle())
                .ogDescription(entity.getOgDescription())
                .ogImageUrl(entity.getOgImageUrl())
                .ogUrl(entity.getOgUrl())
                .ogType(entity.getOgType())
                .activeThemeId(entity.getActiveThemeId())
                .build();
    }

    private CmsSettingDto getDefaultSettingDto(Long schoolId) {
        return CmsSettingDto.builder()
                .schoolId(schoolId)
                .websiteLogo("https://images.unsplash.com/photo-1599305445671-ac291c95aaa9?w=300&auto=format&fit=crop&q=80")
                .favIcon("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=100&auto=format&fit=crop&q=80")
                .contactEmail("contact@nlet.in")
                .complainEmail("complaint@nlet.in")
                .footerText("Copyright © 2026 NLET School. All rights reserved.")
                .schoolAppEnabled(true)
                .frontWebsiteEnabled(true)
                .themePrimaryColor("#1b2a47")
                .themeStandardColor("#dc2626")
                .activeThemeId(1)
                .build();
    }
}

