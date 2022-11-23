package com.ssafy.pocketfolio.db.repository;

import com.ssafy.pocketfolio.db.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByUser_UserSeqOrderByUpdatedDesc(long userSeq);
    List<Room> findAllByRoomSeqIn(List<Long> roomSeqs);
    @Query(value = "SELECT room_seq FROM room WHERE privacy = 'O' AND room_seq != ?1 ORDER BY rand() LIMIT 1", nativeQuery = true)
    Long findRoomSeqByPrivacyOrderByRandom(long roomSeq);
    @Query(value = "SELECT room_seq FROM room WHERE privacy = 'O' AND room_seq != ?1 AND user_seq != ?2 ORDER BY rand() LIMIT 1", nativeQuery = true)
    Long findRoomSeqByPrivacyOrderByRandom(long roomSeq, long userSeq);
    List<Room> findRoomByUser_UserSeqAndIsMain(long userSeq, String isMain);
    Integer countRoomsByUser_UserSeq(long userSeq);

    @Query(value = "SELECT * FROM `room` AS `r` JOIN `user` AS `u` ON `r`.`user_seq` = `u`.`user_seq` WHERE `is_main` = 'T' AND `r`.`user_seq` = ?1 ;", nativeQuery = true)
    Room findRoomByIsMainAndUser_UserSeq(long userSeq);

    @Query(value = "SELECT `r`.*, `rc`.`category_seq` AS categorySeq FROM `room` AS `r` " +
            "JOIN `room_category` AS `rc` ON `r`.`room_seq` = `rc`.`room_seq` " +
            "JOIN `room_like` AS `rl` ON `rc`.`room_seq` = `rl`.`room_seq` " +
            "GROUP BY `r`.`room_seq`, `rc`.`category_seq` " +
            "HAVING categorySeq = ?1 " +
            "ORDER BY count(`rl`.`user_seq`) DESC LIMIT 8 ; ", nativeQuery = true)
    List<Room> findAllByCategorySeq(long categorySeq);
    // 좋아요 한 방 8개
    @Query(value = "SELECT `r`.* FROM `room` AS `r` JOIN `room_like` AS `rl` ON `r`.`room_seq` = `rl`.`room_seq` WHERE `rl`.`user_seq` = ?1 ORDER BY rand() LIMIT 8;", nativeQuery = true)
    List<Room> findRoomLikeByUser_UserSeqOrderByRandom(long userSeq);
    // 팔로우 한 사람 방 8개
    @Query(value = "SELECT `r`.* FROM `room` AS `r` JOIN (SELECT `user_to` FROM `follow` WHERE `user_from` = ?1) AS `f` ON `r`.`user_seq` = `f`.`user_to` WHERE `r`.`is_main` = 'T' ORDER BY rand() LIMIT 8 ;", nativeQuery = true)
    List<Room> findFollowByUser_UserSeqFromOrderByRandom(long userSeq);
    @Query(value = "SELECT `r`.* FROM `room` AS `r` \n" +
            "JOIN `room_like` AS `rl` ON `r`.`room_seq` = `rl`.`room_seq` \n" +
            "GROUP BY `r`.`room_seq` ORDER BY count(`rl`.`user_seq`) DESC LIMIT ?1", nativeQuery = true)
    List<Room> findBestRoom(int limit);
}
