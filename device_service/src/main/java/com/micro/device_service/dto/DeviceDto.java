            package com.micro.device_service.dto;

            import com.micro.device_service.model.DeviceType;

            import lombok.AllArgsConstructor;
            import lombok.Builder;
            import lombok.Data;
            import lombok.NoArgsConstructor;

            @Data
            @Builder
            @AllArgsConstructor
            @NoArgsConstructor
            public class DeviceDto {

                private Long id;            
                private String deviceName;
                private DeviceType deviceType;
                private String location;
                private Long userId;
            }
