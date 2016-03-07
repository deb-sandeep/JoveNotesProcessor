 {
     "preferences": {
         "jove_notes.showHiddenChapters": true,
         "jove_notes.showOnlySelectedRows": true,
         "jove_notes.showHiddenExercises": true
     },
     "dashboardContent": [<syllabuses:{syllabus| 
         {
             "syllabusName": "<syllabus.syllabusName>",
             "subjects" : [<syllabus.subjectList:{subject|
                 {
                    "subjectName" : "<subject.subjectName>",
                    "chapters" : [<subject.chapters:{chapter|
                        {
                         "chapterId": <chapter.chapterId>,
                         "chapterNum": <chapter.chapterNum>,
                         "subChapterNum": <chapter.subChapterNum>,
                         "chapterName": "<chapter.chapterName>",
                         "isNotesAuthorized": false,
                         "isFlashcardAuthorized": false,
                         "isStatisticsAuthorized": false,
                         "isDeleteAuthorized": false,
                         "totalCards": 0,
                         "notStartedCards": 0,
                         "l0Cards": 0,
                         "l1Cards": 0,
                         "l2Cards": 0,
                         "l3Cards": 0,
                         "masteredCards": 0,
                         "numSSRMaturedCards": 0,
                         "isHidden": false,
                         "isDeselected": false                        
                        \}
                    }; separator=",\n">                ]
                 \}
              }; separator=",\n">            ]
         \}
     }; separator=",\n">        ]
 }
