create table user_insight (
    username varchar(255) primary key not null
    constraint fk_user_insight_users references users,
    leadership_style_analysis text,
    animal_spirit_guide text,
    leadership_tip text,
    personal_growth_tip text

);
