package exception;

import com.digitalcopyright.common.BizCodeEnum;
import com.digitalcopyright.utils.R;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice(basePackages = "com.digitalcopyright.controller")
public class UserExceptionControllerAdvice {

    // 处理参数校验异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        Map<String, String> errorMap = new HashMap<>();
        // 提取校验失败的字段信息
        bindingResult.getFieldErrors().forEach((fieldError) -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), BizCodeEnum.VAILD_EXCEPTION.getMsg())
                .put("data", errorMap);
    }

    // 处理其他所有异常
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }

    // 处理NullPointerException异常
    @ExceptionHandler(value = NullPointerException.class)
    public R handleNullPointerException(NullPointerException e) {
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "空指针异常，请检查代码逻辑");
    }

    // 处理其他特定异常（比如参数异常）
    @ExceptionHandler(value = IllegalArgumentException.class)
    public R handleIllegalArgumentException(IllegalArgumentException e) {
        return R.error(BizCodeEnum.BAD_DOING.getCode(), "非法参数，请检查输入");
    }
}
