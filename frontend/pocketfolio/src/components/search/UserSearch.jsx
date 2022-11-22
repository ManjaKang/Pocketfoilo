import React, {useState, useEffect, useRef} from 'react';
import {useDispatch, useSelector} from 'react-redux';
import {unfollowFunc, followFunc} from '../../store/oauthSlice';
import {getUserInfo} from '../../store/oauthSlice';

import {
  UserCard,
  UserItem,
  UserContainer,
  UserImgContainer,
  UserImg,
  UserInfoContainer,
  Icon,
  IconDiv,
  AlreadyFollowIcon,
  FollowIcon,
  UserNameDiv,
  UserDescDiv,
  FollowDiv,
  UserFollowDiv,
} from './UserSearch.style';

import UserProfile from '../common/UserProfile';

const User = ({
  userSeq,
  name,
  profilePic,
  describe,
  followerTotal,
  followingTotal,
  hasFollowed,
  user,
}) => {
  const dispatch = useDispatch();
  const [userModal, setUserModal] = useState(false); //프로필 모달 보이게 안보이게
  const [follow, setFollow] = useState(() => hasFollowed);
  const flag = useRef();

  // 모달 닫는 함수
  const closeUserModal = e => {
    setUserModal(false);
  };

  // 팔로우, 언팔로우
  const handleFollow = async (userSeq, follow) => {
    if (user) {
      if (follow) {
        const {payload} = await dispatch(unfollowFunc(userSeq));
        if (payload) {
          setFollow(false);
          flag.current = true;
        }
      } else {
        const {payload} = await dispatch(followFunc(userSeq));
        if (payload) {
          setFollow(true);
          flag.current = true;
        }
      }
    }
  };

  // 이미지 오류인 경우 기본 이미지 보이게
  const onErrorImg = e => {
    e.target.src = '/assets/images/logo3.png';
  };

  // 특정 유저정보 담을 상태
  const [userInfo, setUserInfo] = useState([]);

  // 특정 유저정보 조회 함수
  const bringUserInfo = async userSeq => {
    const {payload} = await dispatch(getUserInfo(userSeq));
    setUserInfo(payload);
  };

  useEffect(() => {
    getUserInfo();
  }, []);

  return (
    <UserItem>
      {/* 팔로우 버튼 */}
      {/* 팔로우 | 로그인한 상태이고, 방 주인이 아닌 경우 가능 */}
      {!user ? <Icon></Icon> : null}
      {user && user.userSeq !== userSeq ? (
        <Icon>
          <IconDiv
            className="follow"
            onClick={e =>
              handleFollow(
                userSeq,
                (flag.current && follow) || (!flag.current && hasFollowed),
              )
            }
          >
            {(flag.current && follow) || (!flag.current && hasFollowed) ? (
              <AlreadyFollowIcon />
            ) : (
              <FollowIcon />
            )}
          </IconDiv>
        </Icon>
      ) : (
        <Icon>
          <IconDiv />
        </Icon>
      )}
      <UserContainer>
        {/* 사용자 프로필 사진 */}
        <UserImgContainer
          onClick={e => {
            setUserModal(userSeq);
            bringUserInfo(userSeq);
          }}
        >
          <UserImg
            onError={onErrorImg}
            src={profilePic ? profilePic : '/assets/images/user.png'}
          />
        </UserImgContainer>
        {userSeq === userModal && (
          <UserProfile userInfo={userInfo} closeUserModal={closeUserModal} />
        )}
        {/* 유저 정보 */}
        <UserInfoContainer>
          <UserNameDiv>{name}</UserNameDiv>
          {/* <UserDescDiv>{describe}</UserDescDiv> */}
          <FollowDiv>
            <UserFollowDiv>팔로우 | {followerTotal}</UserFollowDiv>
            <UserFollowDiv>팔로잉 | {followingTotal}</UserFollowDiv>
          </FollowDiv>
        </UserInfoContainer>
      </UserContainer>
    </UserItem>
  );
};

const UserSearch = ({data}) => {
  const user = useSelector(state => state.oauth.user);

  useEffect(() => {
    getUserInfo();
  }, []);

  return (
    <>
      <UserCard>
        {data.map(props => {
          return <User {...props} user={user} />;
        })}
      </UserCard>
    </>
  );
};

export default UserSearch;
