package com.ssafy.pocketfolio.api.service;

import com.ssafy.pocketfolio.api.dto.RoomDto;
import com.ssafy.pocketfolio.api.dto.request.RoomArrangeReq;
import com.ssafy.pocketfolio.api.dto.request.RoomReq;
import com.ssafy.pocketfolio.api.dto.response.*;
import com.ssafy.pocketfolio.api.util.MultipartFileHandler;
import com.ssafy.pocketfolio.db.entity.*;
import com.ssafy.pocketfolio.db.repository.*;
import com.ssafy.pocketfolio.db.view.HitStatListView;
import com.ssafy.pocketfolio.db.view.UserView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RoomRepository roomRepository;
    private final RoomCategoryRepository roomCategoryRepository;
    private final RoomHitRepository roomHitRepository;
    private final RoomLikeRepository roomLikeRepository;
    private final PortfolioRepository portfolioRepository;
    private final ItemRepository itemRepository;
    private final ArrangeRepository arrangeRepository;
    private final MultipartFileHandler fileHandler;
    private final FollowRepository followRepository;
    private final TagRepository tagRepository;
    private final RelationRepository relationRepository;

    private final int BEST_LIMIT = 12;

    @Override
    @Transactional
    public Long insertRoom(long userSeq, RoomReq req, MultipartFile thumbnail) throws IOException {
        log.debug("[POST] Service - insertRoom");
        // 저장된 썸네일 주소
        String thumbnailUrl = null;

        // 저장할 썸네일 파일이 있다면
        if (thumbnail != null) {
            thumbnailUrl = fileHandler.saveFile(thumbnail, "room/thumbnail");
            if(thumbnailUrl == null) {
                log.error("썸네일 저장 실패");
                return null;
            }
        }

        if(req.getIsMain() != null && req.getIsMain().equals("T")) {
            // 저장: 다른 T를 F로 변경하고 T로 해야
            List<Room> originMainList = roomRepository.findRoomByUser_UserSeqAndIsMain(userSeq, "T");
            // 로직 상 하나지만 혹시 모를 에러를 방지해 여러 개 체크
            if (!originMainList.isEmpty()) {
                originMainList.forEach(e -> e.changeIsMain("F"));
            }
        }

        Room room = RoomReq.toEntity(req, thumbnailUrl, userRepository.getReferenceById(userSeq));
        long roomSeq = roomRepository.save(room).getRoomSeq();
        log.debug("저장된 방 번호: " + roomSeq);
        if (roomCategoryRepository.existsByRoom_RoomSeq(roomSeq)) {
            roomCategoryRepository.deleteAllByRoom_RoomSeq(roomSeq);
        }
        RoomCategory roomCategory = RoomCategory.builder()
                .category(categoryRepository.getReferenceById(req.getCategory()))
                .room(roomRepository.getReferenceById(roomSeq))
                .build();
        roomCategoryRepository.save(roomCategory);
        return roomSeq;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomListRes> findMyRoomList(long userSeq) {
        log.debug("[GET] Service - findMyRoomList");
        List<RoomListRes> roomsResList;

        try {
            List<Room> rooms = roomRepository.findAllByUser_UserSeqOrderByUpdatedDesc(userSeq); // 최근 수정된 방을 앞으로
            roomsResList = getRoomListResWithMain(rooms);
        } catch (Exception e) {
            log.error(e.getMessage());
            roomsResList = null;
        }

        return roomsResList;
    }

//    @Override
//    @Transactional(readOnly = true)
//    public Map<String, Object> findRoomAll(long userSeq) {
//        log.debug("[GET] Service - findRoomAll");
//        Map<String, Object> map = new HashMap<>();
//
//        try {
//            List<RoomListRes> roomListResList = getRoomListRes(roomRepository.findAll());
//            List<CategoryRes> categories = categoryRepository.findAll().stream().map(CategoryRes::toDto).collect(Collectors.toList());
//
//            map.put("rooms", roomListResList);
//            map.put("categories", categories);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            map = null;
//        }
//        return map;
//    }

    @Override
    @Transactional
    public Map<String, Object> findRoom(long userSeq, long roomSeq) {
        log.debug("[GET] Service - findRoom");
        Map<String, Object> map = new HashMap<>();

        boolean isGuest = userSeq == 0L; // 방문자 처리

        User user = null;
        if (!isGuest) {
            user = userRepository.findById(userSeq).orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        }
        Room room = roomRepository.findById(roomSeq).orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));
        Optional<RoomCategory> roomCategoryO = roomCategoryRepository.findCategorySeqByRoom_RoomSeq(room.getRoomSeq());

        RoomCategory roomCategory;
        // 카테고리가 비어 있으면 기타 카테고리로 하나 만들어 줌
        roomCategory = roomCategoryO.orElseGet(() -> createRoomCategoryIfNotExist(room));

        log.debug("roomCategory: " + roomCategory);

        long ownerSeq = room.getUser().getUserSeq(); // 주인장 유저 번호
        boolean isMyRoom = userSeq == ownerSeq; // 본인 방 여부

        if ("C".equals(room.getPrivacy()) && !isMyRoom) {
            throw new NotFoundException("비공개인 포켓은 본인 말고는 열람할 수 없습니다.");
        }

        // 본인 방이 아닌 경우 + 당일 방문하지 않은 경우 조회수 1 증가
        if (!isGuest && !isMyRoom && !roomHitRepository.existsRoomHitByUser_UserSeqAndRoom_RoomSeqAndHitDateEquals(userSeq, roomSeq, ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate())) {
            roomHitRepository.save(RoomHit.builder().room(room).user(user).build());
        }

        log.debug(roomCategory.getCategory().toString());
        // 방 정보
        map.put("room", RoomDto.toDto(room, CategoryRes.toDto(roomCategory.getCategory())));
        // 조회수
        map.put("hitCount", roomHitRepository.countAllByRoom_RoomSeq(roomSeq));
        map.put("likeCount", roomLikeRepository.countAllByRoom_RoomSeq(roomSeq));
        // Item
        map.put("follow", followRepository.existsByUserFrom_UserSeqAndUserTo_UserSeq(userSeq, ownerSeq));
        map.put("like", roomLikeRepository.existsByUser_UserSeqAndRoom_RoomSeq(userSeq, roomSeq));

        List<ArrangeRes> arranges = new ArrayList<>();
        List<PortfolioListRes> portfolios = new ArrayList<>(); // findRoom에

        List<Arrange> arrangeList = arrangeRepository.findByRoom_RoomSeqOrderByArrangeSeqDesc(roomSeq);
        HashSet<Long> addedPortSeqSet = new HashSet<>(); // 포트폴리오 중복되지 않게 불러오기
        arrangeList.forEach(arrange -> {
            arranges.add(new ArrangeRes(arrange));
            Portfolio portfolio = arrange.getPortfolio();
            if (portfolio != null) {
                Long portSeq = portfolio.getPortSeq();
                if (!addedPortSeqSet.contains(portSeq)) {
                    List<Tag> tags = tagRepository.findAllByPortfolio_PortSeq(portSeq);
                    addedPortSeqSet.add(portSeq);
                    portfolios.add(PortfolioListRes.toDto(portfolio, tags, arrange.getArrangeSeq()));
                }
            }
        });
        map.put("arranges", arranges);
        map.put("portfolios", portfolios);

        UserView userView = userRepository.findProfileById(ownerSeq).orElseThrow(() -> new IllegalArgumentException("해당 사용자(Pocket owner)를 찾을 수 없습니다."));
        List<Room> roomEntities = roomRepository.findAllByUser_UserSeqOrderByUpdatedDesc(ownerSeq);
        map.put("owner", new UserRes(userView, getRoomListResWithMain(roomEntities)));

        return map;
    }


    @Override
    @Transactional
    public Long updateRoomInfo(long userSeq, long roomSeq, RoomReq req, MultipartFile thumbnail) throws IOException {
        log.debug("[PATCH] Service - updateRoomInfo");
        Room room = roomRepository.findById(roomSeq).orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));
        RoomCategory roomCategory = roomCategoryRepository.findByRoom_RoomSeq(roomSeq);
        log.debug("room category: " + roomCategory.toString());
        if (room.getUser().getUserSeq() != userSeq) {
            log.error("권한 없음");
            return null;
        }

        // 저장된 썸네일 주소
        String thumbnailUrl = room.getThumbnail();

        // 저장할 썸네일 파일이 있다면 thumbnail 수정
        if (thumbnail != null) {
            // 저장된 썸네일 주소가 있으면 해당 썸네일 삭제 후 새로 저장
            if (thumbnailUrl != null) {
                fileHandler.deleteFile(thumbnailUrl, "portfolio/thumbnail");
            }
            thumbnailUrl = fileHandler.saveFile(thumbnail, "room/thumbnail");
            if(thumbnailUrl == null) {
                log.error("썸네일 저장 실패");
                return null;
            }
        } else {
            // 썸네일 삭제 후 전송되고 이전에 썸네일 있었으면 썸네일 파일 삭제
            if (thumbnailUrl != null) {
                fileHandler.deleteFile(thumbnailUrl, "portfolio/thumbnail");
            }
        }

        try {
            room.updateRoom(req.getName(), thumbnailUrl);
            roomCategory.updateCategory(categoryRepository.getReferenceById(req.getCategory()));
            if (req.getPrivacy() != null) {
                room.updatePrivacy(req.getPrivacy());
            }
            if(req.getIsMain() != null && req.getIsMain().equals("T") && room.getIsMain().equals("F")) {
                // update (다른 T를 F로 변경하고 T로 해야)
                List<Room> originMainList = roomRepository.findRoomByUser_UserSeqAndIsMain(userSeq, "T");
                // 로직 상 하나지만 혹시 모를 에러를 방지해 여러 개 체크
                if (!originMainList.isEmpty()) {
                    originMainList.forEach(e -> e.changeIsMain("F"));
                }
                room.changeIsMain(req.getIsMain());
            }

            log.debug("저장된 방 번호: " + roomSeq);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return roomSeq;
    }

    @Override
    @Transactional
    public Long updateRoom(long userSeq, long roomSeq, RoomArrangeReq roomArrangeReq) { // TODO: (나중에) 한 방에는 포트폴리오를 한 개까지만 등록할 수 있게 하기
        // 수정하는 메소드. 성능 상 삭제 후 삽입도 고려해 볼 만함.
        Room room = roomRepository.findById(roomSeq).orElseThrow(() -> new IllegalArgumentException("해당 포켓이 존재하지 않습니다."));
        if (room.getUser().getUserSeq() != userSeq) {
            throw new IllegalArgumentException("요청한 유저와 수정하려는 포켓의 주인이 일치하지 않습니다.");
        }
        HashSet<Long> arrangeSeqSet = arrangeRepository.findArrangeSeqByRoom_RoomSeq(roomSeq); // 리스트만 되면 변환 필요

        room.updateTheme(roomArrangeReq.getTheme());

        log.debug("테마 업데이트 성공");

        roomArrangeReq.getArranges().forEach(arrangeReq -> {

            Portfolio portfolio = null;
            Long portSeq = arrangeReq.getPortSeq();

            if (portSeq != null) {
                try {
                    portfolio = portfolioRepository.getReferenceById(portSeq);
                } catch (EntityNotFoundException e) {
                    log.error("포트폴리오 번호를 찾지 못하여 포트폴리오 연결 없이 진행");
                }
            }

            Long arrangeSeq = arrangeReq.getArrangeSeq();
            if (arrangeSeq != null && arrangeSeqSet.contains(arrangeSeq)) { // 원래 있던 Arrange (UPDATE)
                arrangeRepository.findById(arrangeSeq).get().updateArrangeAll(arrangeReq.getLocation(), arrangeReq.getRotation(), portfolio);
                arrangeSeqSet.remove(arrangeSeq);
            } else { // 기존에 없던 Arrange (INSERT)
                Arrange arrange = arrangeReq.toEntity(room, itemRepository.getReferenceById(arrangeReq.getItemSeq()), portfolio);
                arrangeRepository.save(arrange);
            }
        });

        arrangeSeqSet.forEach(arrangeRepository::deleteById);

        return room.getRoomSeq();
    }

    @Override
    @Transactional
    public Boolean deleteRoom(long userSeq, long roomSeq) {
        log.debug("[DELETE] Service - deleteRoom");
        Room room = roomRepository.findById(roomSeq).orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));

        if (room.getUser().getUserSeq() != userSeq) {
            log.error("권한 없음");
            return false;
        }

        if (roomRepository.countRoomsByUser_UserSeq(userSeq) == 1) {
            log.error("최소 1개의 방은 있어야 함");
            return false;
        }

        // 썸네일 삭제
        if (room.getThumbnail() != null) {
            fileHandler.deleteFile(room.getThumbnail(), "portfolio/thumbnail");
        }

        roomRepository.deleteById(roomSeq);
        return true;
    }

    @Override
    @Transactional
    public Boolean insertRoomLike(long userSeq, long roomSeq) {
        log.debug("[POST] Service - insertRoomLike");

        User user = userRepository.findById(userSeq).orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        Room room = roomRepository.findById(roomSeq).orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));
        log.debug("user: " + user.toString());
        log.debug("room: " + room.toString());

        // 본인 방이 아닌 경우 + 좋아요 이력 없는 경우 좋아요
        if (userSeq != room.getUser().getUserSeq()) {
            log.debug("..");

            if (roomLikeRepository.existsByUser_UserSeqAndRoom_RoomSeq(userSeq, roomSeq)) {
                log.error("이미 좋아요 추가한 방");
                return false;
            } else {
                roomLikeRepository.save(RoomLike.builder().room(room).user(user).build());
                return true;
            }
        }
        log.error("본인 방");
        return false;
    }

    @Override
    @Transactional
    public Boolean deleteRoomLike(long userSeq, long roomSeq) {
        log.debug("[DELETE] Service - deleteRoomLike");

        Room room = roomRepository.findById(roomSeq).orElseThrow(() -> new IllegalArgumentException("해당 방을 찾을 수 없습니다."));

        // 본인 방이 아닌 경우 + 좋아요 이력 있는 경우 좋아요
        if (userSeq != room.getUser().getUserSeq()) {
            if (!roomLikeRepository.existsByUser_UserSeqAndRoom_RoomSeq(userSeq, roomSeq)) {
                log.error("좋아요 이력 없음");
                return false;
            } else {
                try {
                    roomLikeRepository.deleteByRoom_RoomSeqAndUser_UserSeq(roomSeq, userSeq);
                    log.debug("좋아요 취소 완료");
                    return true;
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true) // 지연 조회 시점까지 세션 유지
    public List<RoomListRes> findRoomLikeList(long userSeq) {
        log.debug("[GET] Service - findRoomLikeList");
        try {
            List<Room> rooms = roomLikeRepository.findAllByUser_UserSeq(userSeq).stream().map(RoomLike::getRoom).collect(Collectors.toList());
            return getRoomListRes(rooms, userSeq);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomListRes> findRoomBestList(long userSeq) {
        log.debug("[GET] Service - findRoomBestList");
        try {
            List<Room> rooms = roomRepository.findBestRoom(BEST_LIMIT);
            return getRoomListRes(rooms, userSeq);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long findRandomRoom(long roomSeq, long userSeq) {
        log.debug("[GET] Service - findRandomRoom");
        if (userSeq == 0) {
            return roomRepository.findRoomSeqByPrivacyOrderByRandom(roomSeq);
        } else {
            return roomRepository.findRoomSeqByPrivacyOrderByRandom(roomSeq, userSeq);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> findGuestList(long roomSeq) {
        log.debug("[GET] Service - findGuestList");
        try {
            Map<String, Object> map = new HashMap<>();
            List<HitStatListView> hitStatListViews = roomHitRepository.countAllByHitDateAndRoomSeq(roomSeq);
            List<GuestRes> guests = roomHitRepository.findGuest(roomSeq).stream().map(GuestRes::toDto).collect(Collectors.toList());
            map.put("guests", guests);
            map.put("today", roomHitRepository.countRoomHitToday(roomSeq));
            map.put("hitStat", HitStatRes.toDto(hitStatListViews));
            return map;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryRes> findCategoryList() {
        log.debug("[GET] Service - findCategoryList");
        try {
            return categoryRepository.findAll().stream().map(CategoryRes::toDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MainRoomRes findMainRoom(long userSeq) {
        log.debug("[GET] Service - findMainRoom");
        try {
            Room room = roomRepository.findRoomByIsMainAndUser_UserSeq(userSeq);
            int like = roomLikeRepository.countAllByRoom_RoomSeq(room.getRoomSeq()).intValue();
            int hit = roomHitRepository.countAllByRoom_RoomSeq(room.getRoomSeq()).intValue();
            List<ArrangeRes> arranges = arrangeRepository.findByRoom_RoomSeqOrderByArrangeSeqDesc(room.getRoomSeq())
                    .stream().map(ArrangeRes::new).collect(Collectors.toList());
            return MainRoomRes.toDto(room, arranges, like, hit);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MainPocketListRes> findMainRandomRoom(long userSeq, String type) {
        log.debug("[GET] Service - findMainRoomList");
        List<MainPocketListRes> pocketList = new ArrayList<>();
        try {
            List<Room> rooms = new ArrayList<>();
            switch (type) {
                case "like":
                    rooms = roomRepository.findRoomLikeByUser_UserSeqOrderByRandom(userSeq);
                    break;
                case "follow":
                    rooms = roomRepository.findFollowByUser_UserSeqFromOrderByRandom(userSeq);
                    break;
                case "recommend":
                    Relation relation = relationRepository.findByUserSeq_UserSeq(userSeq);
                    List<Long> roomSeqs = Arrays.stream(relation.getRoomList().split(",")).map(Long::parseLong).collect(Collectors.toList());
                    rooms = roomRepository.findAllByRoomSeqIn(roomSeqs);
                    break;
            }
            for (Room room : rooms) {
                boolean follow = followRepository.existsByUserFrom_UserSeqAndUserTo_UserSeq(userSeq, room.getUser().getUserSeq());
                boolean like = roomLikeRepository.existsByUser_UserSeqAndRoom_RoomSeq(userSeq, room.getRoomSeq());
                String category = categoryRepository.findCategoryByRoomSeq(room.getRoomSeq());
                int likeCount = roomLikeRepository.countAllByRoom_RoomSeq(room.getRoomSeq()).intValue();
                int hitCount = roomHitRepository.countAllByRoom_RoomSeq(room.getRoomSeq()).intValue();
                pocketList.add(MainPocketListRes.toDto(room, category, follow, like, likeCount, hitCount, room.getUser()));
            }
            return pocketList;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryRecRes> findCategoryRecList(long userSeq) {
        // 카테고리 목록 불러오기
        List<CategoryRes> categoryList = categoryRepository.findAll().stream().map(CategoryRes::toDto).collect(Collectors.toList());
        // 카테고리별 추천 목록 담을 리스트
        List<CategoryRecRes> categoryRecList = new ArrayList<>();
        // 카테고리 개수
        int size = categoryList.size();
        for(int category_seq = 1 ; category_seq <= size ; category_seq++) {
            // 카테고리 이름
            String name = categoryList.get(category_seq-1).getName();
            // 카테고리별 포켓 담을 리스트
            List<MainPocketListRes> pocketList = new ArrayList<>();
            // 카테고리별 방 개수
            List<Room> rooms = roomRepository.findAllByCategorySeq(category_seq);

            for (Room room : rooms) {
                boolean follow = followRepository.existsByUserFrom_UserSeqAndUserTo_UserSeq(userSeq, room.getUser().getUserSeq());
                boolean like = roomLikeRepository.existsByUser_UserSeqAndRoom_RoomSeq(userSeq, room.getRoomSeq());
                int likeCount = roomLikeRepository.countAllByRoom_RoomSeq(room.getRoomSeq()).intValue();
                int hitCount = roomHitRepository.countAllByRoom_RoomSeq(room.getRoomSeq()).intValue();
                pocketList.add(MainPocketListRes.toDto(room, name, follow, like, likeCount, hitCount, room.getUser()));
            }
            categoryRecList.add(CategoryRecRes.toDto(name, pocketList));
        }
        return categoryRecList;
    }

    private List<RoomListRes> getRoomListRes(List<Room> rooms, long userSeq) { // TODO: 이것도 조인으로 할 수 있지 않을까 1
        return rooms.stream().map(room -> {
            String category = categoryRepository.findCategoryByRoomSeq(room.getRoomSeq());
            int like = roomLikeRepository.countAllByRoom_RoomSeq(room.getRoomSeq()).intValue();
            int hit = roomHitRepository.countAllByRoom_RoomSeq(room.getRoomSeq()).intValue();
            boolean isFollowing = followRepository.existsByUserFrom_UserSeqAndUserTo_UserSeq(userSeq, room.getUser().getUserSeq());
            return RoomListRes.toDto(room, category, like, hit, isFollowing);
        }).collect(Collectors.toList());
    }

    private List<RoomListRes> getRoomListResWithMain(List<Room> rooms) { // TODO: 이것도 조인으로 할 수 있지 않을까 1
        List<RoomListRes> roomList = new ArrayList<>();
        rooms.forEach(room -> {
            String category = categoryRepository.findCategoryByRoomSeq(room.getRoomSeq());
            int like = roomLikeRepository.countAllByRoom_RoomSeq(room.getRoomSeq()).intValue();
            int hit = roomHitRepository.countAllByRoom_RoomSeq(room.getRoomSeq()).intValue();
            if ("T".equals(room.getIsMain())) {
                roomList.add(0, RoomListRes.toDto(room, category, like, hit, false));
            } else {
                roomList.add(RoomListRes.toDto(room, category, like, hit, false));
            }
        });
        return roomList;
    }

    private RoomCategory createRoomCategoryIfNotExist(Room room) {
        return roomCategoryRepository.save(RoomCategory.builder().room(room).category(categoryRepository.getReferenceById(1L)).build());
    }
}
