import React, {useState, useEffect} from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import {useDispatch, useSelector} from 'react-redux';
import {getSearch} from '../../store/searchSlice';

import Nav from '../common/Nav';
import {
  Container,
  Container1,
  SearchInput,
  SearchIcon,
  Tag,
  TagContainer,
  TopButtonIcon,
  TopButton,
  Select,
  SelectOption,
  FilterDiv,
  DivTest,
  Tab,
  SelectBox,
  Tabs,
} from './Search.style';

import PocketSearch from './PocketSearch';
import PortSearch from './PortSearch';
import UserSearch from './UserSearch';

// 임시데이터(tag)
const tags = [
  '개발',
  '사운드',
  '게임 디자인',
  '미술',
  '광고',
  '패션',
  '제품 디자인',
  'UI/UX',
  '일러스트레이션',
  '그래픽 디자인',
  '기타',
];

// filter options
const filterOptions = [
  {value: 1, name: '좋아요순'},
  {value: 2, name: '조회순'},
  {value: 3, name: '팔로우순'},
];

// filter component
const Filter = props => {
  const handleChange = e => {
    props.setSort(e.target.value);
  };

  return (
    <SelectBox>
      <Select onChange={handleChange}>
        {props.options.map(option => (
          <SelectOption
            value={option.value}
            defaultValue={props.defaultValue === option.value}
            key={option.value}
          >
            {option.name}
          </SelectOption>
        ))}
      </Select>
    </SelectBox>
  );
};

const Search = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();

  // 카테고리
  const [category, setCategory] = useState(2 ** tags.length - 1);
  // 카테고리(3개: 포켓, 포트폴리오, 유저)
  const [searchMode, setSearchMode] = useState('room');
  // 정렬 필터
  const [sort, setSort] = useState(1);
  // 페이지당 보이는 개수
  const size = 20;
  // 검색 결과가 저장되는 상태
  const [data, setData] = useState([]);
  // 검색어
  const [word, setWord] = useState(location.state.search);
  // 페이지
  const [page, setPage] = useState(1);

  /** params: 이진수 */
  const selectCategory = (e, bin_int) => {
    if (category & bin_int) {
      setCategory(category - bin_int);
    } else {
      setCategory(category + bin_int);
    }
  };

  const selectSearchMode = (e, searchMode) => {
    setSearchMode(searchMode);
  };

  // 검색 요청 함수
  const getData = async () => {
    const params = {
      search: location.state.search,
      sort: location.state.sort,
      category: location.state.category,
      size: size,
      page: location.state.page,
    };
    const {payload} = await dispatch(getSearch({params, searchMode}));
    setData(payload.list);
  };

  // 스크롤시 데이터 불러오는 함수
  const getDataScroll = async () => {
    const params = {
      search: location.state.search,
      sort: location.state.sort,
      category: location.state.category,
      size: size,
      page,
    };
    const {payload} = await dispatch(getSearch({params, searchMode}));
    setData([...data, ...payload.list]);
  };

  // 입력창 변화 감지
  const onChange = e => {
    setWord(e.target.value);
  };

  // 검색어 창 입력
  const onSubmit = async event => {
    event.preventDefault();
    navigate('/search', {
      state: {
        search: word,
        sort: sort,
        category: category,
        size: size,
        page: 1,
      },
    });
  };

  // 검색어 창 엔터시 입력
  const keyDownHandler = event => {
    if (event.key === 'Enter') {
      setWord(word);
      onSubmit(event);
    }
  };

  // 최상단 이동 버튼
  const clickHandlerTop = e => {
    // 이미 최상단일 경우 그냥 return
    if (!window.scrollY) return;

    window.scrollTo({top: 0, behavior: 'smooth'});
  };

  // 스크롤 test
  const handleScroll = () => {
    const scrollHeight = document.documentElement.scrollHeight;
    const scrollTop = document.documentElement.scrollTop;
    const clientHeight = document.documentElement.clientHeight;

    if (scrollTop + clientHeight >= scrollHeight) {
      setPage(prev => prev + 1);
    }
  };

  useEffect(() => {
    getDataScroll();
  }, [page]);

  useEffect(() => {
    window.addEventListener('scroll', handleScroll);
    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, []);
  

  // 좋아요
  const handleLike = roomSeq => {
    setData(
      data.map(dataItem => {
        if (dataItem.roomSeq === roomSeq) {
          return {...dataItem, like: dataItem.like + 1, isLiked: true};
        } else {
          return dataItem;
        }
      }),
    );
  };

  // 좋아요 취소
  const handleDisLike = roomSeq => {
    setData(
      data.map(dataItem => {
        if (dataItem.roomSeq === roomSeq) {
          return {...dataItem, like: dataItem.like - 1, isLiked: false};
        } else {
          return dataItem;
        }
      }),
    );
  };

  // 내비게이트 발생 시 검색
  useEffect(() => {
    getData();
  }, [location]);

  // 카테고리 변경 시 검색
  useEffect(() => {
    if (category === 0) {
      setCategory(2 ** (tags.length - 1));
    }
    navigate('/search', {
      state: {
        search: word,
        sort: sort,
        category: category,
        size: size,
        page: 1,
      },
    });
  }, [category]);

  // 마이포켓, 포트폴리오, 유저 변경 시 검색
  useEffect(() => {
    navigate('/search', {
      state: {
        search: word,
        sort: sort,
        category: category,
        size: size,
        page: 1,
      },
    });
  }, [searchMode]);

  // 정렬 변경 시 검색
  useEffect(() => {
    navigate('/search', {
      state: {
        search: word,
        sort: sort,
        category: category,
        size: size,
        page: 1,
      },
    });
  }, [sort]);

  return (
    <>
      {/* 네브바 */}
      <Nav />
      {/* top 버튼 */}
      <TopButton onClick={clickHandlerTop}>
        <TopButtonIcon />
      </TopButton>
      {/* 검색창 */}
      <Container1>
        <Container>
          <SearchIcon />
          <SearchInput
            placeholder="검색어를 입력해주세요"
            onKeyDown={keyDownHandler}
            onChange={onChange}
            value={word}
          />
        </Container>
        {/* 카테고리 */}
        <Tabs>
          <Tab
            searchMode={searchMode}
            name={'room'}
            onClick={e => selectSearchMode(e, 'room')}
          >
            마이포켓
          </Tab>
          <Tab
            searchMode={searchMode}
            name={'portfolio'}
            onClick={e => selectSearchMode(e, 'portfolio')}
          >
            포트폴리오
          </Tab>
          <Tab
            searchMode={searchMode}
            name={'user'}
            onClick={e => selectSearchMode(e, 'user')}
          >
            유저
          </Tab>
        </Tabs>
      </Container1>
      {/* 태그 */}
      {searchMode === 'room' ? (
        <TagContainer>
          <Tag
            style={
              category === 2 ** tags.length - 1
                ? {
                    backgroundColor: '#e75452',
                    color: '#fff',
                    border: '1px solid #fff',
                  }
                : {
                    backgroundColor: '#fff',
                    color: 'darkgray',
                    border: '1px solid darkgray',
                  }
            }
            onClick={() => {
              if (category === 2 ** tags.length - 1) {
                setCategory(2 ** (tags.length - 1));
              } else {
                setCategory(2 ** tags.length - 1);
              }
            }}
          >
            ALL
          </Tag>
          {tags.map((tag, idx) => {
            return (
              <Tag
                category={category}
                onClick={e => selectCategory(e, 2 ** (tags.length - idx - 1))}
                key={idx}
                style={
                  !!((2 ** (tags.length - idx - 1)) & category)
                    ? {
                        backgroundColor: '#e75452',
                        color: '#fff',
                        border: '1px solid #fff',
                      }
                    : {
                        backgroundColor: '#fff',
                        color: 'darkgray',
                        border: '1px solid darkgray',
                      }
                }
              >
                {tag}
              </Tag>
            );
          })}
        </TagContainer>
      ) : null}
      <DivTest>
        {/* 포켓 검색 */}
        {searchMode === 'room' && data ? (
          <>
            {/* 필터 */}
              <FilterDiv>
                <Filter options={filterOptions} setSort={setSort} />
                <PocketSearch
                  data={data}
                  handleLike={handleLike}
                  handleDisLike={handleDisLike}
                />
              </FilterDiv>
          </>
        ) : null}
        {/* 포트폴리오 검색 */}
        {searchMode === 'portfolio' && data ? (
          <>
            <FilterDiv>
              <Filter options={filterOptions} setSort={setSort} />
              <PortSearch               
                data={data}
                handleLike={handleLike}
                handleDisLike={handleDisLike}
              />
            </FilterDiv>
          </>
        ) : null}
        {/* 유저 검색 */}
        {searchMode === 'user' && data ? <UserSearch data={data}/> : null}
      </DivTest>
    </>
  );
};

export default Search;
