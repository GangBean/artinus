package com.artinus.subscription.api.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import com.artinus.subscription.api.entity.CellPhoneNumber;
import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.ChannelAuth;
import com.artinus.subscription.api.entity.ChannelAuthSet;
import com.artinus.subscription.api.entity.Member;
import com.artinus.subscription.api.entity.SubscriptionState;
import com.artinus.subscription.api.repository.ChannelRepository;
import com.artinus.subscription.api.repository.MemberRepository;

@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    public ApplicationRunner loadTestData(MemberRepository memberRepository, ChannelRepository channelRepository) {
        return (ApplicationArguments args) -> {
            memberRepository.save(Member.builder()
                    .cellPhoneNumber(CellPhoneNumber.from("010-1111-1111"))
                    .subscriptionState(SubscriptionState.NONE)
                    .build());
            memberRepository.save(Member.builder()
                    .cellPhoneNumber(CellPhoneNumber.from("010-2222-2222"))
                    .subscriptionState(SubscriptionState.NORMAL)
                    .build());
            memberRepository.save(Member.builder()
                    .cellPhoneNumber(CellPhoneNumber.from("010-3333-3333"))
                    .subscriptionState(SubscriptionState.PREMIUM)
                    .build());

            channelRepository.save(Channel.builder()
                    .name("WEB")
                    .auths(ChannelAuthSet.of(ChannelAuth.SUBSCRIBE, ChannelAuth.CANCLE))
                    .build());
            channelRepository.save(Channel.builder()
                    .name("MOBILE")
                    .auths(ChannelAuthSet.of(ChannelAuth.SUBSCRIBE))
                    .build());
            channelRepository.save(Channel.builder()
                    .name("APP")
                    .auths(ChannelAuthSet.of(ChannelAuth.CANCLE))
                    .build());
            channelRepository.save(Channel.builder()
                    .name("TMP")
                    .auths(null)
                    .build());
        };
    }
}
