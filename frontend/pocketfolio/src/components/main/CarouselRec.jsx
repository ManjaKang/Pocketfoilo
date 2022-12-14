import {useRef} from 'react';
import {useState} from 'react';
import {useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {
  Container,
  Title,
  CarouselContainer,
  ItemList,
  ItemContainer,
  ImgBox,
  Img,
  NextIcon,
  PrevDiv,
  PrevIcon,
  NextDiv,
  InfoBox,
  Name,
  LikeHitDiv,
  IconDiv,
  LikeIcon,
  Count,
  HitIcon,
  Shadow,
} from './CarouselRec.style';

const CarouselRec = ({rec, idx}) => {
  const navigate = useNavigate();
  const icon = [
    'π§‘',
    'π',
    'π',
    'π',
    'π',
    'π€',
    'π€',
    'π€',
    'π',
    'π',
    'π',
    'π',
  ];

  const slideRef = useRef();
  const totalRef = useRef();
  const [totalCnt, setTotalCnt] = useState(0);
  const [currentCnt, setCurrentCnt] = useState(0);

  // Next λ²νΌ ν΄λ¦­ μ
  const NextSlide = () => {
    if (currentCnt >= totalCnt) {
      setCurrentCnt(0);
    } else {
      setCurrentCnt(currentCnt + 1);
    }
  };

  // Prev λ²νΌ ν΄λ¦­ μ
  const PrevSlide = () => {
    if (currentCnt === 0) {
      setCurrentCnt(totalCnt); // λ§μ§λ§ μ¬μ§μΌλ‘ λμ΄κ°λλ€.
    } else {
      setCurrentCnt(currentCnt - 1);
    }
  };

  // μ μ²΄ μμ΄ν κ°μ κ°μ Έμ€κΈ°
  useEffect(() => {

    if (
      currentCnt === 0 &&
      slideRef.current.scrollWidth <= totalRef.current.clientWidth
    ) {
      setTotalCnt(0);
      setCurrentCnt(0);
    } else {
      setTotalCnt(rec.recommend.length - 1);
    }
  }, [rec, slideRef.current?.scrollWidth, totalRef]);

  useEffect(() => {
    const item = document.querySelector('.item');
    slideRef.current.style.transition = 'all 0.5s ease-in-out';


    if (
      totalRef.current.clientWidth + currentCnt * item.offsetWidth <=
      slideRef.current.scrollWidth + item.offsetWidth
    ) {
      slideRef.current.style.transform = `translateX(-${
        currentCnt * item.offsetWidth
      }px)`;
    } else {
      setCurrentCnt(0);
    }
  }, [currentCnt]);

  return (
    <Container>
      <Title>{`${icon[idx]} "${rec.name}"μμ κ°μ₯ μΈκΈ°μλ ν¬μΌ`}</Title>

      <CarouselContainer ref={totalRef}>
        {currentCnt !== 0 && (
          <PrevDiv className="navigation" onClick={PrevSlide}>
            <PrevIcon />
          </PrevDiv>
        )}

        {currentCnt !== totalCnt && (
          <NextDiv className="navigation" onClick={NextSlide}>
            <NextIcon />
          </NextDiv>
        )}

        <ItemList ref={slideRef}>
          {rec.recommend.map((item, idx) => (
            <ItemContainer
              key={idx}
              className="item"
              onClick={() => navigate(`/room/${item.roomSeq}`)}
            >
              <ImgBox>
                <Img
                  src={
                    item.thumbnail
                      ? item.thumbnail
                      : process.env.PUBLIC_URL + '/assets/images/room_01.PNG'
                  }
                  onError={e => {
                    e.target.src =
                      process.env.PUBLIC_URL + '/assets/images/logo3.png';
                  }}
                  alt="μΈλ€μΌ"
                />
                <LikeHitDiv className="icon">
                  <IconDiv>
                    <LikeIcon />
                  </IconDiv>
                  <Count>{item.likeCount}</Count>
                  <IconDiv>
                    <HitIcon />
                  </IconDiv>
                  <Count>{item.hitCount}</Count>
                </LikeHitDiv>
              </ImgBox>

              <InfoBox>
                <Name>{item.roomName}</Name>
              </InfoBox>

              <Shadow className="shadow" />
            </ItemContainer>
          ))}
        </ItemList>
      </CarouselContainer>
    </Container>
  );
};

export default CarouselRec;
