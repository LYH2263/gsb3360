package com.example.campustrade.service.impl;

import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.User;
import com.example.campustrade.mapper.ItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void listAll_returnsMapperResult() {
        Item item = new Item();
        item.setId(1L);
        when(itemMapper.findAll()).thenReturn(List.of(item));

        List<Item> result = itemService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(itemMapper, times(1)).findAll();
    }

    @Test
    void listPage_usesOffsetAndLimit() {
        Item item = new Item();
        item.setId(5L);
        when(itemMapper.findPage(5, 5)).thenReturn(List.of(item));

        List<Item> result = itemService.listPage(2, 5);

        assertThat(result).hasSize(1);
        verify(itemMapper, times(1)).findPage(5, 5);
    }

    @Test
    void countAll_delegatesToMapper() {
        when(itemMapper.countAll()).thenReturn(10L);
        long count = itemService.countAll();
        assertThat(count).isEqualTo(10L);
        verify(itemMapper, times(1)).countAll();
    }

    @Test
    void getById_delegatesToMapper() {
        Item item = new Item();
        item.setId(2L);
        when(itemMapper.findById(2L)).thenReturn(item);

        Item result = itemService.getById(2L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        verify(itemMapper, times(1)).findById(2L);
    }

    @Test
    void create_setsCreatedAt_andDefaultStatus_thenInserts() {
        Item item = new Item();
        item.setTitle("test");
        item.setPrice(new BigDecimal("12.34"));

        User user = new User();
        user.setId(100L);
        user.setRole("USER");
        itemService.create(item, user);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemMapper, times(1)).insert(captor.capture());
        Item inserted = captor.getValue();

        assertThat(inserted.getCreatedAt()).isNotNull();
        assertThat(inserted.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(inserted.getStatus()).isEqualTo("ACTIVE");
        assertThat(inserted.getSellerId()).isEqualTo(100L);
    }

    @Test
    void create_keepsProvidedStatus() {
        Item item = new Item();
        item.setTitle("test");
        item.setPrice(new BigDecimal("12.34"));
        item.setStatus("SOLD");

        User user = new User();
        user.setId(101L);
        user.setRole("USER");
        itemService.create(item, user);

        verify(itemMapper, times(1)).insert(any(Item.class));
        assertThat(item.getStatus()).isEqualTo("SOLD");
        assertThat(item.getCreatedAt()).isNotNull();
        assertThat(item.getSellerId()).isEqualTo(101L);
    }

    @Test
    void update_delegatesToMapper() {
        Item item = new Item();
        item.setId(9L);
        item.setTitle("updated");
        item.setPrice(new BigDecimal("1.00"));

        Item existing = new Item();
        existing.setId(9L);
        existing.setSellerId(200L);
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));
        existing.setStatus("ACTIVE");
        when(itemMapper.findById(9L)).thenReturn(existing);

        User admin = new User();
        admin.setId(1L);
        admin.setRole("ADMIN");
        itemService.update(item, admin);

        verify(itemMapper, times(1)).update(item);
    }

    @Test
    void deleteById_delegatesToMapper() {
        Item existing = new Item();
        existing.setId(7L);
        existing.setSellerId(300L);
        when(itemMapper.findById(7L)).thenReturn(existing);

        User admin = new User();
        admin.setId(1L);
        admin.setRole("ADMIN");
        itemService.deleteById(7L, admin);
        verify(itemMapper, times(1)).deleteById(7L);
    }

    @Test
    void update_whenNotOwnerAndNotAdmin_throwsAccessDenied() {
        Item existing = new Item();
        existing.setId(8L);
        existing.setSellerId(999L);
        existing.setCreatedAt(LocalDateTime.now());
        when(itemMapper.findById(8L)).thenReturn(existing);

        User user = new User();
        user.setId(100L);
        user.setRole("USER");

        Item update = new Item();
        update.setId(8L);
        update.setTitle("x");
        update.setPrice(new BigDecimal("1.00"));

        assertThatThrownBy(() -> itemService.update(update, user))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void delete_whenNotOwnerAndNotAdmin_throwsAccessDenied() {
        Item existing = new Item();
        existing.setId(6L);
        existing.setSellerId(999L);
        when(itemMapper.findById(6L)).thenReturn(existing);

        User user = new User();
        user.setId(100L);
        user.setRole("USER");

        assertThatThrownBy(() -> itemService.deleteById(6L, user))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void completeTrade_whenActiveAndNotOwner_marksSold() {
        Item existing = new Item();
        existing.setId(10L);
        existing.setSellerId(200L);
        existing.setStatus("ACTIVE");
        when(itemMapper.findById(10L)).thenReturn(existing);

        User buyer = new User();
        buyer.setId(201L);
        buyer.setRole("USER");

        itemService.completeTrade(10L, buyer);

        verify(itemMapper, times(1)).markAsSold(eq(10L), eq(201L), any(LocalDateTime.class));
    }

    @Test
    void completeTrade_whenOwner_throwsIllegalArgument() {
        Item existing = new Item();
        existing.setId(11L);
        existing.setSellerId(300L);
        existing.setStatus("ACTIVE");
        when(itemMapper.findById(11L)).thenReturn(existing);

        User buyer = new User();
        buyer.setId(300L);

        assertThatThrownBy(() -> itemService.completeTrade(11L, buyer))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
