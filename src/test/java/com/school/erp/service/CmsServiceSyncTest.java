package com.school.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.dto.cms.*;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.cms.*;
import com.school.erp.repository.cms.*;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.cms.CmsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsServiceSyncTest {
    @Mock CmsEventRepository eventRepository;
    @Mock CmsGalleryRepository galleryRepository;
    @Mock CmsNoticeRepository noticeRepository;
    @Mock CmsPageRepository pageRepository;
    @Mock CmsMenuGroupRepository menuGroupRepository;
    @Mock CmsMenuItemRepository menuItemRepository;
    @Mock CmsBannerRepository bannerRepository;
    @Mock CmsMediaRepository mediaRepository;
    @Mock CmsTestimonialRepository testimonialRepository;
    @Mock CmsSettingRepository settingRepository;
    @Spy ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks CmsService service;

    @AfterEach
    void tearDown() {
        AuthContextHolder.clear();
    }

    @Test
    void galleryCaptionsAndLegacyUrlsSurviveReload() {
        when(galleryRepository.save(any())).thenAnswer(call -> {
            CmsGallery entity = call.getArgument(0);
            entity.setId(12L);
            when(galleryRepository.findById(12L)).thenReturn(Optional.of(entity));
            return entity;
        });
        var created = service.createGallery(CmsGalleryDto.builder().schoolId(7L).title("Sports")
                .photos(List.of(new CmsGalleryPhotoDto(null, "data:image/png;base64,abc", "Relay final")))
                .seoTitle("Sports day").build());
        assertEquals(List.of("data:image/png;base64,abc"), created.getGalleryImages());
        var reloaded = service.getGalleryById(12L);
        assertEquals("Relay final", reloaded.getPhotos().get(0).getCaption());
        assertNotNull(reloaded.getPhotos().get(0).getId());
        assertEquals("Sports day", reloaded.getSeoTitle());
        assertEquals(7L, reloaded.getSchoolId());
    }

    @Test
    void legacyAlbumsReadAsPhotosAndCanClearPhotos() {
        var entity = CmsGallery.builder().id(12L).title("Legacy").galleryImages("[\"https://school.test/a.jpg\"]").build();
        when(galleryRepository.findById(12L)).thenReturn(Optional.of(entity));
        when(galleryRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals("https://school.test/a.jpg", service.getGalleryById(12L).getPhotos().get(0).getUrl());
        var saved = service.updateGallery(12L, CmsGalleryDto.builder().title("Legacy").photos(List.of()).build());
        assertTrue(saved.getPhotos().isEmpty());
        assertTrue(saved.getGalleryImages().isEmpty());
    }

    @Test
    void bannerTargetLinkPersistsInEntityAndResponse() {
        when(bannerRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        var response = service.createBanner(CmsBannerDto.builder().schoolId(7L).fileName("Admissions")
                .url("data:image/png;base64,abc").targetLink("/site/pages/admissions").build());
        assertEquals("/site/pages/admissions", response.getTargetLink());
        assertEquals(7L, response.getSchoolId());
    }

    @Test
    void blankSearchUsesIndexedSchoolQueriesAndNeverPassesNullToLikeQueries() {
        when(eventRepository.findBySchoolIdOrderByIdDesc(7L)).thenReturn(List.of());
        when(galleryRepository.findBySchoolIdOrderByIdDesc(7L)).thenReturn(List.of());
        when(noticeRepository.findBySchoolIdOrderByIdDesc(7L)).thenReturn(List.of());
        when(pageRepository.findBySchoolIdOrderByIdDesc(7L)).thenReturn(List.of());
        when(menuGroupRepository.findBySchoolIdOrderByIdAsc(7L)).thenReturn(List.of());
        when(bannerRepository.findBySchoolIdOrderByIdDesc(7L)).thenReturn(List.of());
        when(mediaRepository.findBySchoolIdOrderByIdDesc(7L)).thenReturn(List.of());
        when(testimonialRepository.findBySchoolIdOrderByIdDesc(7L)).thenReturn(List.of());

        service.getAllEvents(7L, null);
        service.getAllEvents(7L, "   ");
        service.getAllGalleries(7L, null);
        service.getAllNotices(7L, "");
        service.getAllPages(7L, null);
        service.getAllMenus(7L, "");
        service.getAllBanners(7L, null, "all");
        service.getAllMedia(7L, " ", "Select");
        service.getAllTestimonials(7L, null);

        verify(eventRepository, never()).searchEvents(any(), any());
        verify(galleryRepository, never()).searchGalleries(any(), any());
        verify(noticeRepository, never()).searchNotices(any(), any());
        verify(pageRepository, never()).searchPages(any(), any());
        verify(menuGroupRepository, never()).searchMenuGroups(any(), any());
        verify(bannerRepository, never()).searchBanners(any(), any(), any());
        verify(mediaRepository, never()).searchMedia(any(), any(), any());
        verify(testimonialRepository, never()).searchTestimonials(any(), any());
    }

    @Test
    void nonBlankSearchTrimsQueryAndResolvesSchoolIdFromAuthContext() {
        AuthContextHolder.set(new AuthenticatedUser(99L, 7L, UserRole.ADMIN));
        when(noticeRepository.searchNotices(7L, "Olympiad")).thenReturn(List.of());
        when(mediaRepository.findBySchoolIdAndFileTypeIgnoreCaseOrderByIdDesc(7L, "image")).thenReturn(List.of());

        service.getAllNotices(null, "  Olympiad ");
        service.getAllMedia(null, "", "image");

        verify(noticeRepository).searchNotices(7L, "Olympiad");
        verify(mediaRepository).findBySchoolIdAndFileTypeIgnoreCaseOrderByIdDesc(7L, "image");
    }

    @Test
    void settingsFallbackUsesGlobalTemplateRatherThanAnotherSchoolRow() {
        when(settingRepository.findFirstBySchoolIdOrderByIdAsc(7L)).thenReturn(Optional.empty());
        when(settingRepository.findFirstBySchoolIdIsNullOrderByIdAsc())
                .thenReturn(Optional.of(CmsSetting.builder().id(1L).schoolId(null).contactEmail("template@school.test").build()));

        CmsSettingDto settings = service.getSettings(7L);

        assertEquals(7L, settings.getSchoolId());
        assertEquals("template@school.test", settings.getContactEmail());
        verify(settingRepository, never()).findFirstByOrderByIdAsc();
    }
}
