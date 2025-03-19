package com.example.sixt.configs;

import org.modelmapper.Conditions;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
//        return new ModelMapper();
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setAmbiguityIgnored(true)
                .setPropertyCondition(Conditions.isNotNull());

        Converter<String, Long> stringToLongConverter = new Converter<String, Long>() {
            @Override
            public Long convert(MappingContext<String, Long> context) {
                try {
                    return context.getSource() == null ? null : Long.parseLong(context.getSource());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        };

        modelMapper.addConverter(stringToLongConverter);

        return modelMapper;
    }
}
