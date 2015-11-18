package kz.telecom.happydrive.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Galymzhan Sh on 11/7/15.
 */
public class Category {
    public final int id;
    public final String name;

    Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Category categoryById(int id) {
        List<Category> categoryList = getCategoriesListTemp();
        for (Category cat : categoryList) {
            if (cat.id == id) {
                return cat;
            }
        }

        return null;
    }

    @NonNull
    public static List<Category> getCategoriesListTemp() {
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category(0, "Не выбрано"));
        categoryList.add(new Category(1, "IT/Интернет/Телеком"));
        categoryList.add(new Category(2, "Автомобильный бизнес"));
        categoryList.add(new Category(3, "Административный персонал"));
        categoryList.add(new Category(4, "Банки/Инвестиции/Лизинг"));
        categoryList.add(new Category(5, "Исскуство/Развлечения/Масс-медиа"));
        categoryList.add(new Category(6, "Консультирование"));
        categoryList.add(new Category(7, "Логистика/Транспортировка/Закуп"));
        categoryList.add(new Category(8, "Маркетинг/Реклама"));
        categoryList.add(new Category(9, "Машиностроение/Инжиниринг"));
        categoryList.add(new Category(10, "Медицинские услуги"));
        categoryList.add(new Category(11, "Менеджмент"));
        categoryList.add(new Category(12, "Наука/Образование"));
        categoryList.add(new Category(13, "Обучение/тренинг"));
        categoryList.add(new Category(14, "Охрана/Безопасность"));
        categoryList.add(new Category(15, "Поддержка"));
        categoryList.add(new Category(16, "Продажи"));
        categoryList.add(new Category(17, "Сельское хозяйство"));
        categoryList.add(new Category(18, "Стажировка"));
        categoryList.add(new Category(19, "Страхование"));
        categoryList.add(new Category(20, "Туризм/Гостиница/Рестораны"));
        categoryList.add(new Category(21, "Управление персоналом"));
        categoryList.add(new Category(22, "Финансы/Бухгалтерия"));
        categoryList.add(new Category(23, "Фитнес/Спорт/Косметология"));
        categoryList.add(new Category(24, "Юриспруденция"));

        return categoryList;
    }
}
