package az.corbank.mscorbank.adapter.out.persistence.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RedisHash("RefreshToken")
public class RefreshToken {

    @Id
    String id;

    @Indexed
    String email;

    @TimeToLive
    Long ttl;
}