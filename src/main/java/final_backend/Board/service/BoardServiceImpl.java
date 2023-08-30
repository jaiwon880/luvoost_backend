package final_backend.Board.service;

import final_backend.Board.model.CursorResult;
import final_backend.Member.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import final_backend.Board.model.Board;
import final_backend.Board.respository.BoardRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;

    public BoardServiceImpl(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public Long getDefaultCursorId() {
        return boardRepository.findTopByOrderByBidDesc().getBid()+1;
    }

    @Override
    public Long getAllBoards() {
        return boardRepository.count();
    }

    @Override
    public Optional<Board> getBoardById(Long bid) {
        return boardRepository.findById(bid);
    }

    @Override
    public CursorResult<Board> getBoard(Long cursorId, Pageable page, String findStr, String findingMethod){
        if (cursorId==null){cursorId=getDefaultCursorId();}
        System.out.println("getBoard:"+findingMethod);
        System.out.println("getBoard:"+findStr);
        final List<Board> boards = getBoardList(cursorId, page, findStr, findingMethod);
        final Long lastIdOfList = boards.isEmpty() ?
                null : boards.get(boards.size() - 1).getBid();
        return new CursorResult<Board>(boards, hasNext(lastIdOfList));
    }

    @Override
    public CursorResult<Board> getBoard(Long cursorId, Pageable page){
        final List<Board> boards = getBoardList(cursorId, page);
        final Long lastIdOfList = boards.isEmpty() ?
                null : boards.get(boards.size() - 1).getBid();

        return new CursorResult<Board>(boards, hasNext(lastIdOfList));
    }

    @Override
    public List<Board> getBoardList(Long cursorId, Pageable page, String findStr, String findingMethod) {
        System.out.println("getBoardList:"+findingMethod);
        System.out.println("getBoardList:"+findStr);
        List<Board> boards = new ArrayList<>();
        System.out.println(findStr);
        System.out.println(findingMethod);
        if ("1".equals(findingMethod)) {           //닉네임
            boards = boardRepository.findByNickNameContainingAndBidLessThanOrderByBidDesc(findStr, cursorId, page);
        } else if ("2".equals(findingMethod)) {     //제목
            boards = boardRepository.findBybTitleContainingAndBidLessThanOrderByBidDesc(findStr, cursorId, page);
            System.out.println(boards);
        } else if ("3".equals(findingMethod)) {     //내용
            boards = boardRepository.findBybContentContainingAndBidLessThanOrderByBidDesc(findStr, cursorId, page);
        }else if ("4".equals(findingMethod)) {      //내글
            boards = boardRepository.findByNickNameContainingAndBidLessThanOrderByBidDesc(findStr, cursorId, page);
        } else {
            boards = boardRepository.findByBidLessThanOrderByBidDesc(cursorId, page);
        }
        return boards;
    }

    @Override
    public List<Board> getBoardList(Long bid, Pageable page) {
        return bid == null ?
                boardRepository.findAllByOrderByBidDesc(page) :
                boardRepository.findByBidLessThanOrderByBidDesc(bid, page);
    }

    @Override
    public Boolean hasNext(Long bid) {
        if (bid == null) return false;
        return boardRepository.existsByBidLessThan(bid);
    }

//    @Override
//    public Page<Board> getBoardList(Long bid, Pageable page) {
//        return bid == null ?
//                boardRepository.findAllByOrderByBidDesc(page) :
//                boardRepository.findByBidLessThanOrderByBidDesc(bid, page);
//    }


    @Override
    public Board createBoard(Board board) {
        LocalDateTime currentTime = LocalDateTime.now();
        board.setBCreatedAt(currentTime);
        board.setBUpdatedAt(currentTime);
        board.setBViews(0L);
        board.setBRecommendations(0L);
        return boardRepository.save(board);
    }

    @Override
    public void deleteBoard(Long bid) {
        boardRepository.deleteById(bid);
    }

    @Override
    public Board updateBoard(Long bid, Board updatedBoard) {
        Optional<Board> boardOptional = boardRepository.findById(bid);
        if (boardOptional.isPresent()) {
            Board existingBoard = boardOptional.get();
            existingBoard.setBTitle(updatedBoard.getBTitle());
            existingBoard.setBContent(updatedBoard.getBContent());
            existingBoard.setUid(updatedBoard.getUid());
            existingBoard.setBUpdatedAt(LocalDateTime.now());
            return boardRepository.save(existingBoard);
        } else {
            return null;
        }
    }

    @Override
    public Board increaseViewCount(Board board) {
        board.setBViews(board.getBViews() + 1);
        System.out.println(board.getBid()+"조회됨");
        return boardRepository.save(board);
    }

    @Override
    public Board recommendIncrease(Long bid) {
        Optional<Board> boardOptional = boardRepository.findById(bid);
        if (boardOptional.isPresent()) {
            Board existingBoard = boardOptional.get();
            existingBoard.setBRecommendations(existingBoard.getBRecommendations()+1);
            System.out.println(bid +" 번 게시글 추천됨");
            return boardRepository.save(existingBoard);
        } else {
            return null;
        }
    }


//    public List<Board> getMyBoards(String nickname) {
//        List<Board> myboards = boardRepository.findByNickName(nickname);
//        return myboards;
//    }

}