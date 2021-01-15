-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- 主机： mysql
-- 生成日期： 2021-01-15 14:54:30
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

INSERT INTO `news` (`id`, `title`, `content`, `created_at`) VALUES
('51528475-9889-45ea-9a8f-4acb2bebf8ba', 'string', 'string', '2021-01-15 22:26:04'),
('67ab85a1-865a-456f-a52e-ef0ba21a70f5', 'string', 'string', '2021-01-15 22:26:03'),
('88e45b68-aefa-4257-b6dd-bf2b49e718e5', 'string', 'string', '2021-01-15 22:26:01'),
('8e9c1196-de88-4274-9ae4-7ab659625aea', '要不要啊', '8989', '2021-01-15 22:26:02');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
