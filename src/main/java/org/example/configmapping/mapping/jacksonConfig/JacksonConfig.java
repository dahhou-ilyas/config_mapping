package org.example.configmapping.mapping.jacksonConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.example.configmapping.mapping.transformers.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class JacksonConfig {
    @Bean
    @Qualifier("yamlMapper")
    public ObjectMapper yamlMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());


        mapper.registerModule(new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);

                context.registerSubtypes(
                        DateFormatTransformer.class,
                        EnumMappingTransformer.class,
                        PrefixTransformer.class,
                        StringTruncateTransformer.class,
                        StringCastTransformer.class,
                        DefaultValueTransformer.class,
                        BooleanToFlagTransformer.class,
                        UpperCaseTransformer.class,
                        LowerCaseTransformer.class,
                        RegexReplaceTransformer.class
                );
            }
        });

        return mapper;
    }

}
