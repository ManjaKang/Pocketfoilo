import {useEffect, useRef, useReducer, Suspense} from 'react';
import {useNavigate} from 'react-router-dom';
import {
  Frame,
  Outer,
  Divider,
  Content1,
  Content2,
  Title,
  SubTitle,
  IconBtn,
  DotBtn,
  Dots,
  Description,
} from './Landing.style';
import {Button} from '../common/Button';
import {BsChevronDoubleDown} from 'react-icons/bs';
import LandingCanvas from './LandingCanvas';

const _active = {
  opacity: '1',
  transition: 'opacity 500ms',
};

const _gray = {
  opacity: '0.5',
  transition: 'opacity 500ms',
};

const _hidden = {
  opacity: '0',
  visibility: 'hidden',
  transition: 'opacity 500ms , visibility 500ms',
};

const Landing = () => {
  const outerDivRef = useRef();
  const navigate = useNavigate();

  const reducer = (state, action) => {
    const pageHeight = window.innerHeight;
    const DIVIDER_HEIGHT = 5;
    outerDivRef.current.scrollTo({
      top: pageHeight * (action - 1) + DIVIDER_HEIGHT * (action - 1),
      left: 0,
      behavior: 'smooth',
    });
    return action;
  };
  const [page, scrollPage] = useReducer(reducer, 1);
  useEffect(() => {
    const outerDivRefCurrent = outerDivRef.current;
    outerDivRefCurrent.addEventListener('wheel', wheelHandler);
    return () => {
      outerDivRefCurrent.removeEventListener('wheel', wheelHandler);
    };
  }, []);

  const wheelHandler = e => {
    e.preventDefault();
    const {deltaY} = e;
    const {scrollTop} = outerDivRef.current; // 스크롤 위쪽 끝부분 위치
    const pageHeight = window.innerHeight; // 화면 세로길이, 100vh와 같습니다.

    if (deltaY > 0) {
      // 스크롤 내릴 때
      if (scrollTop >= 0 && scrollTop < pageHeight) {
        //현재 1페이지
        scrollPage(2);
      } else if (scrollTop >= pageHeight && scrollTop < pageHeight * 2) {
        //현재 2페이지
        scrollPage(3);
      } else if (scrollTop >= pageHeight && scrollTop < pageHeight * 3) {
        //현재 3페이지
        scrollPage(4);
      } else {
        // 현재 4페이지
        scrollPage(4);
      }
    } else {
      // 스크롤 올릴 때
      if (scrollTop >= 0 && scrollTop < pageHeight) {
        //현재 1페이지
        scrollPage(1);
      } else if (scrollTop >= pageHeight && scrollTop < pageHeight * 2) {
        //현재 2페이지
        scrollPage(1);
      } else if (scrollTop >= pageHeight && scrollTop < pageHeight * 3) {
        //현재 3페이지
        scrollPage(2);
      } else {
        // 현재 4페이지
        scrollPage(3);
      }
    }
  };
  const handleBtn = (e, page) => scrollPage(page);
  const handleNaviBtn = e => {
    navigate('/main');
  };

  return (
    <Outer ref={outerDivRef}>
      <Suspense fallback={null}>
        <LandingCanvas outerDivRef={outerDivRef} page={page} />
      </Suspense>
      <Dots style={page === 1 ? _hidden : _active}>
        <DotBtn style={_gray} onClick={e => handleBtn(e, 1)}></DotBtn>
        <DotBtn
          style={page === 2 ? _active : _gray}
          onClick={e => handleBtn(e, 2)}
        ></DotBtn>
        <DotBtn
          style={page === 3 ? _active : _gray}
          onClick={e => handleBtn(e, 3)}
        ></DotBtn>
        <DotBtn
          style={page === 4 ? _active : _gray}
          onClick={e => handleBtn(e, 4)}
        ></DotBtn>
      </Dots>
      <Frame>
        <Content1>
          <Title>손쉽게 만드는 나만의 포트폴리오 전시장</Title>
          <Button onClick={handleNaviBtn}>바로 시작하기</Button>
          <IconBtn
            style={page === 1 ? _active : _hidden}
            onClick={e => handleBtn(e, 2)}
          >
            <BsChevronDoubleDown></BsChevronDoubleDown>
          </IconBtn>
        </Content1>
      </Frame>
      <Divider />
      <Frame>
        <Content2>
          <SubTitle>
            설치가 필요없는 <br /> 포트폴리오 툴
          </SubTitle>
          <Description>
            언제 어디서나 손쉽게 꾸밀 수 있는 <br />
            3D 포트폴리오를 만들어보세요
          </Description>
        </Content2>
      </Frame>
      <Divider />
      <Frame>
        <Content2>
          {' '}
          <SubTitle>
            상상하는 대로 그려지는
            <br />
            나만의 공간
          </SubTitle>
          <Description>
            다양한 테마에 당신의 상상을 더해 <br />
            멋진 공간을 꾸며보세요
          </Description>
        </Content2>
      </Frame>
      <Divider />
      <Frame>
        <Content2>
          {' '}
          <SubTitle>
            내 손 안의 <br />
            작은 전시회
          </SubTitle>
          <Description>
            내 포트폴리오를 전시하고, <br />
            다른 사람들의 포트폴리오도 구경해보세요
          </Description>
          <Button onClick={handleNaviBtn}>바로 시작하기</Button>
        </Content2>
      </Frame>
    </Outer>
  );
};

export default Landing;
