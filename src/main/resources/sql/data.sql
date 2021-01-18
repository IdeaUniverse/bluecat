-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- 主机： mysql
-- 生成日期： 2021-01-17 15:24:28
-- 服务器版本： 5.7.26
-- PHP 版本： 7.4.6

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `blue_cat`
--

--
-- 转存表中的数据 `news`
--

INSERT INTO `news` (`id`, `title`, `content`, `liked_num`, `created_at`) VALUES
('51528475-9889-45ea-9a8f-4acb2bebf8ba', '不办5G套餐也能用5G', '近日，工信部明确一些5G服务问题：(1)只要有5G手机，不办5G套餐也能用5G，但使用5G套餐可享受更快网络服务；(2)未办5G套餐时用5G流量从4G套餐扣除；(3)4G手机受网络制式无法使用5G网络服务', 0, '2021-01-15 22:26:04'),
('67ab85a1-865a-456f-a52e-ef0ba21a70f5', '南方气温将开启速降模式', '据中央气象台，受寒潮影响，预计1月16日至17日，青藏高原中东部、西南地区大部、江南中南部及华南等地日平均或最低气温下降6～10摄氏度，其中，云南东部、贵州大部和江南西南部等地降温可达10摄氏度以上。', 0, '2021-01-15 22:26:03'),
('88e45b68-aefa-4257-b6dd-bf2b49e718e5', '卧龙发布全球唯一白色大熊猫影像', '1月15日，大熊猫国家公园管理局对外发布了去年2月拍到的，全球唯一一只白色大熊猫野外活动的影像。自2019年5月红外线相机首次在野外记录到这只白色大熊猫以来，截至2020年底，四川卧龙野外布控的红外相机仅在去年2月份有两次拍摄到...', 0, '2021-01-15 22:26:01');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
