package com.liligo.reggie.dto;

import com.liligo.reggie.entity.Setmeal;
import com.liligo.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
