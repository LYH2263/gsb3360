package com.example.campustrade.controller;

import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.User;
import com.example.campustrade.service.ItemService;
import com.example.campustrade.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private UserService userService;

    private void mockCurrentUser(String username, long id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        when(userService.findByUsername(username)).thenReturn(user);
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void getItems_returnsListView_withItemsModel() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        Item item = new Item();
        item.setId(1L);
        item.setTitle("x");
        item.setPrice(new BigDecimal("1.00"));
        item.setCreatedAt(LocalDateTime.now());
        when(itemService.countActive()).thenReturn(1L);
        when(itemService.listActivePage(1, 10)).thenReturn(List.of(item));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items/list"))
                .andExpect(model().attributeExists("items", "page", "totalPages", "totalItems"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void getItems_withPageAndSize_usesThoseValues() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        when(itemService.countActive()).thenReturn(20L);
        when(itemService.listActivePage(2, 10)).thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("items/list"))
                .andExpect(model().attributeExists("items", "page", "totalPages", "totalItems"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void getMine_returnsMineView() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        when(itemService.countBySellerId(2L)).thenReturn(12L);
        when(itemService.listBySellerPage(2L, 1, 10)).thenReturn(List.of());

        mockMvc.perform(get("/items/mine"))
                .andExpect(status().isOk())
                .andExpect(view().name("items/mine"))
                .andExpect(model().attributeExists("items"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void getOrders_returnsOrdersView() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        when(itemService.countByBuyerId(2L)).thenReturn(8L);
        when(itemService.listByBuyerPage(2L, 1, 10)).thenReturn(List.of());

        mockMvc.perform(get("/items/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("items/orders"))
                .andExpect(model().attributeExists("items"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void getNew_returnsFormView() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        mockMvc.perform(get("/items/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("items/form"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void getDetail_whenNotFound_redirectsToList() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        when(itemService.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/items/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void getDetail_whenFound_returnsDetailView() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        Item item = new Item();
        item.setId(3L);
        item.setTitle("detail");
        item.setPrice(new BigDecimal("3.00"));
        item.setCreatedAt(LocalDateTime.now());
        when(itemService.getById(3L)).thenReturn(item);

        mockMvc.perform(get("/items/3"))
                .andExpect(status().isOk())
                .andExpect(view().name("items/detail"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void getEdit_whenNotFound_redirectsToList() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        when(itemService.getById(42L)).thenReturn(null);

        mockMvc.perform(get("/items/42/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void getEdit_whenFound_returnsFormView() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        Item item = new Item();
        item.setId(2L);
        item.setSellerId(2L);
        item.setTitle("t");
        item.setPrice(new BigDecimal("2.00"));
        when(itemService.getById(2L)).thenReturn(item);

        mockMvc.perform(get("/items/2/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("items/form"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void getEdit_whenNotOwner_redirectsForbidden() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        Item item = new Item();
        item.setId(2L);
        item.setSellerId(999L);
        item.setTitle("t");
        item.setPrice(new BigDecimal("2.00"));
        when(itemService.getById(2L)).thenReturn(item);

        mockMvc.perform(get("/items/2/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?forbidden"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void postCreate_whenInvalid_returnsFormWithErrors() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        mockMvc.perform(post("/items")
                        .param("title", "")
                        .param("price", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("items/form"))
                .andExpect(model().attributeHasFieldErrors("item", "title", "price"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void postCreate_whenValid_redirectsToList_andCallsService() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        doNothing().when(itemService).create(any(Item.class), any(User.class));

        mockMvc.perform(post("/items")
                        .param("title", "new item")
                        .param("description", "desc")
                        .param("price", "12.34")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));

        verify(itemService).create(any(Item.class), any(User.class));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void postUpdate_whenInvalid_returnsFormWithErrors() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        mockMvc.perform(post("/items/5")
                        .param("title", "")
                        .param("price", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("items/form"))
                .andExpect(model().attributeHasFieldErrors("item", "title", "price"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void postUpdate_whenValid_redirectsToList_andCallsService() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        doNothing().when(itemService).update(any(Item.class), any(User.class));

        mockMvc.perform(post("/items/5")
                        .param("title", "updated")
                        .param("description", "d")
                        .param("price", "9.99")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));

        verify(itemService).update(any(Item.class), any(User.class));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void postDelete_redirectsToList_andCallsService() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        doNothing().when(itemService).deleteById(eq(3L), any(User.class));

        mockMvc.perform(post("/items/3/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));

        verify(itemService).deleteById(eq(3L), any(User.class));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    void postTrade_redirectsToDetail_andCallsService() throws Exception {
        mockCurrentUser("alice", 2L, "USER");
        doNothing().when(itemService).completeTrade(eq(6L), any(User.class));

        mockMvc.perform(post("/items/6/trade"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/6"));

        verify(itemService).completeTrade(eq(6L), any(User.class));
    }
}
