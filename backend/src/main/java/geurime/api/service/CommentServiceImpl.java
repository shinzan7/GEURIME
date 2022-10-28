package geurime.api.service;

import geurime.database.entity.Board;
import geurime.database.entity.Comment;
import geurime.database.repository.BoardRepository;
import geurime.database.repository.CommentRepository;
import geurime.database.repository.UserRepository;
import geurime.exception.CustomException;
import geurime.exception.CustomExceptionList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    /**
     * 댓글을 작성하고 생성된 댓글의 id를 반환한다.
     * @param request
     * @return 생성된 댓글의 id
     */
    @Override
    public Long createComment(Comment.CommentPostRequest request) {
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new CustomException(CustomExceptionList.BOARD_NOT_FOUND_ERROR));
        userRepository.findById(request.getCommentUserId())
                .orElseThrow(() -> new CustomException(CustomExceptionList.USER_NOT_FOUND_ERROR));

        Comment comment = Comment.builder()
                .board(board)
                .commentUserId(request.getCommentUserId())
                .createTime(LocalDateTime.now())
                .commentContent(request.getCommentContent())
                .build();
        commentRepository.save(comment);

        return comment.getId();
    }

    /**
     * 댓글작성자가 댓글을 수정한다. 댓글작성자가 아닌 경우에는 수정하지 않는다.
     * @param request
     * @return 수정된 댓글의 id (댓글작성자가 아니면 0)
     */
    @Override
    public Long updateComment(Comment.CommentPutRequest request) {
        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new CustomException(CustomExceptionList.COMMENT_NOT_FOUND_ERROR));
        if(comment.getCommentUserId() == request.getUserId()){
            comment.updateComment(request.getCommentContent());
            return comment.getId();
        }else {
            return 0L;
        }
    }

    /**
     * 댓글작성자가 댓글을 삭제한다. 댓글작성자가 아닌 경우에는 삭제하지 않는다.
     * @param userId 삭제를 요청하는 유저 id
     * @param commentId
     * @return 삭제되었다면 true, 아닌 경우는 false
     */
    @Override
    public Boolean deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CustomExceptionList.COMMENT_NOT_FOUND_ERROR));
        if(comment.getCommentUserId() == userId){
            commentRepository.delete(comment);
            return true;
        }else {
            return false;
        }
    }
}
