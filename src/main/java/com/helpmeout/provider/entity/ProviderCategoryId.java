package com.helpmeout.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderCategoryId implements Serializable {
    private Long providerProfile;
    private Long category;
}