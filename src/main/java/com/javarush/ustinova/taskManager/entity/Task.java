package com.javarush.ustinova.taskManager.entity;

import com.javarush.ustinova.taskManager.entity.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    //    id (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE) //айдишник нельзя менять
    private Long id;

    //    title
    @Column(nullable = false)
    private String title;

    //    description
    @Column(columnDefinition = "TEXT") //создать эту колонку именно как TEXT, а не как VARCHAR(255)
    private String description;

    //    deadline
    @Column(nullable = false)
    private LocalDate deadline;



    //    status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;


    //    user_id (FK -> USER.id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;



}
