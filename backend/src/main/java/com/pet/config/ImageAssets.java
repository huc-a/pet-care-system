package com.pet.config;
import java.util.Map;
/** 种子图片（本地静态托管于 /img/，随 jar 发布）与业务对象的对应关系 */
public class ImageAssets {
    public static final Map<String, String> AVATAR_BY_USERNAME = Map.ofEntries(
        Map.entry("admin", "/img/avatar-admin.jpg"),
        Map.entry("owner", "/img/avatar-owner.jpg"),
        Map.entry("owner2", "/img/avatar-owner2.jpg"),
        Map.entry("owner3", "/img/avatar-owner3.jpg"),
        Map.entry("sitter", "/img/avatar-sitter.jpg"),
        Map.entry("sitter2", "/img/avatar-sitter2.jpg"),
        Map.entry("sitter3", "/img/avatar-sitter3.jpg"),
        Map.entry("sitter4", "/img/avatar-sitter4.jpg"),
        Map.entry("sitter5", "/img/avatar-sitter5.jpg"),
        Map.entry("sitter6", "/img/avatar-sitter6.jpg"));
    public static final Map<String, String> PET_BY_NAME = Map.ofEntries(
        Map.entry("豆豆", "/img/pet-doudou.jpg"),
        Map.entry("奶茶", "/img/pet-naicha.jpg"),
        Map.entry("团团", "/img/pet-tuantuan.jpg"),
        Map.entry("咕噜", "/img/pet-gulu.jpg"),
        Map.entry("旺财", "/img/pet-wangcai.jpg"),
        Map.entry("雪球", "/img/pet-xueqiu.jpg"),
        Map.entry("阿福", "/img/pet-afu.jpg"),
        Map.entry("芝麻", "/img/pet-zhima.jpg"),
        Map.entry("布丁", "/img/pet-buding.jpg"),
        Map.entry("小橘", "/img/pet-xiaoju.jpg"));
    public static final Map<String, String> PET_BY_BREED = Map.ofEntries(
        Map.entry("金毛", "/img/pet-doudou.jpg"),
        Map.entry("英短", "/img/pet-naicha.jpg"),
        Map.entry("布偶", "/img/pet-tuantuan.jpg"),
        Map.entry("美短", "/img/pet-gulu.jpg"),
        Map.entry("柯基", "/img/pet-wangcai.jpg"),
        Map.entry("比熊", "/img/pet-xueqiu.jpg"),
        Map.entry("边牧", "/img/pet-afu.jpg"),
        Map.entry("柴犬", "/img/pet-zhima.jpg"),
        Map.entry("泰迪", "/img/pet-buding.jpg"),
        Map.entry("中华田园猫", "/img/pet-xiaoju.jpg"));
    public static final Map<String, String> SERVICE_BY_NAME = Map.ofEntries(
        Map.entry("上门喂猫", "/img/svc-feedcat.jpg"),
        Map.entry("上门喂狗", "/img/svc-feeddog.jpg"),
        Map.entry("上门遛狗", "/img/svc-walkdog.jpg"),
        Map.entry("门店寄养-猫", "/img/svc-boardcat.jpg"),
        Map.entry("门店寄养-狗", "/img/svc-boarddog.jpg"),
        Map.entry("家庭寄养", "/img/svc-boardhome.jpg"),
        Map.entry("洗护美容", "/img/svc-bath.jpg"),
        Map.entry("陪玩服务", "/img/svc-play.jpg"),
        Map.entry("喂药护理", "/img/svc-care.jpg"),
        Map.entry("加餐营养餐", "/img/svc-meal.jpg"),
        Map.entry("接送服务", "/img/svc-pickup.jpg"),
        Map.entry("视频陪伴", "/img/svc-video.jpg"),
        Map.entry("上门喂药加项", "/img/svc-medaddon.jpg"));
    public static final Map<String, Integer> SERVICE_SALES = Map.ofEntries(
        Map.entry("上门喂猫", 486), Map.entry("上门喂狗", 352), Map.entry("上门遛狗", 618),
        Map.entry("门店寄养-猫", 264), Map.entry("门店寄养-狗", 198), Map.entry("家庭寄养", 176),
        Map.entry("洗护美容", 423), Map.entry("陪玩服务", 289), Map.entry("喂药护理", 97),
        Map.entry("加餐营养餐", 512), Map.entry("接送服务", 234), Map.entry("视频陪伴", 158),
        Map.entry("上门喂药加项", 86));
    public static final Map<String, String> SPACE_BY_NAME = Map.ofEntries(
        Map.entry("猫咪别墅A区", "/img/space-cata.jpg"),
        Map.entry("阳光犬舍", "/img/space-dogsun.jpg"),
        Map.entry("家庭寄养位", "/img/space-home.jpg"),
        Map.entry("豪华单间", "/img/space-luxury.jpg"),
        Map.entry("猫咪别墅B区", "/img/space-catb.jpg"),
        Map.entry("小型犬乐园", "/img/space-puppark.jpg"));
}
