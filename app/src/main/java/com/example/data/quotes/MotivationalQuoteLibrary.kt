package com.example.data.quotes

object MotivationalQuoteLibrary {

    val quotes: List<MotivationalQuote> = listOf(
        // ================= STUDY (12 quotes) =================
        MotivationalQuote(
            id = "study_01",
            text = "Focus on understanding one concept at a time. Depth beats speed.",
            author = "to-dodo",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "study_02",
            text = "Start with the next paragraph, not the entire textbook.",
            author = "to-dodo reminder",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "study_03",
            text = "Small study sessions today make exam week calm and peaceful.",
            author = "to-dodo",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "study_04",
            text = "Confusion is simply the first stage of deep learning. Keep going.",
            author = "to-dodo reminder",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.AFTERNOON
        ),
        MotivationalQuote(
            id = "study_05",
            text = "Active recall and honest practice outwork hours of passive reading.",
            author = "to-dodo",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "study_06",
            text = "Close the extra tabs. Give this single topic your full attention.",
            author = "to-dodo reminder",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "study_07",
            text = "Curiosity makes hard subjects feel like quiet adventures.",
            author = "to-dodo",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "study_08",
            text = "Even 25 focused minutes moves your knowledge forward.",
            author = "to-dodo",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "study_09",
            text = "Review what you learned today. Solidify the foundation before sleep.",
            author = "to-dodo reminder",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.NIGHT
        ),
        MotivationalQuote(
            id = "study_10",
            text = "Solving problems is how understanding turns into confidence.",
            author = "to-dodo",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "study_11",
            text = "Clear notes and a clear mind. Take it one concept at a time.",
            author = "to-dodo",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "study_12",
            text = "Knowledge compounds quietly over months of everyday curiosity.",
            author = "to-dodo reminder",
            category = QuoteCategory.STUDY,
            suitableTime = TimeContext.ANY
        ),

        // ================= CODING (8 quotes) =================
        MotivationalQuote(
            id = "code_01",
            text = "Break complex logic down into tiny functions that make sense.",
            author = "to-dodo",
            category = QuoteCategory.CODING,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "code_02",
            text = "Every bug solved is another tool added to your problem-solving craft.",
            author = "to-dodo reminder",
            category = QuoteCategory.CODING,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "code_03",
            text = "First make it work, then make it clean. Don't fear the first draft.",
            author = "to-dodo",
            category = QuoteCategory.CODING,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "code_04",
            text = "Great software is built one commit, one edge case, and one test at a time.",
            author = "to-dodo",
            category = QuoteCategory.CODING,
            suitableTime = TimeContext.AFTERNOON
        ),
        MotivationalQuote(
            id = "code_05",
            text = "Read the stack trace patiently. The compiler is trying to guide you.",
            author = "to-dodo reminder",
            category = QuoteCategory.CODING,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "code_06",
            text = "Building real projects is the best documentation you will ever write.",
            author = "to-dodo",
            category = QuoteCategory.CODING,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "code_07",
            text = "Clean code is simply empathy for the person reading it tomorrow.",
            author = "to-dodo",
            category = QuoteCategory.CODING,
            suitableTime = TimeContext.EVENING
        ),
        MotivationalQuote(
            id = "code_08",
            text = "Save your work, commit your progress, and rest your eyes.",
            author = "to-dodo reminder",
            category = QuoteCategory.CODING,
            suitableTime = TimeContext.NIGHT
        ),

        // ================= CONSISTENCY (10 quotes) =================
        MotivationalQuote(
            id = "cons_01",
            text = "Consistency beats intensity. Showing up today is what counts.",
            author = "to-dodo",
            category = QuoteCategory.CONSISTENCY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "cons_02",
            text = "Small steps become lifelong milestones when repeated daily.",
            author = "to-dodo",
            category = QuoteCategory.CONSISTENCY,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "cons_03",
            text = "You don't need a heroic effort today. Just quiet, steady progress.",
            author = "to-dodo reminder",
            category = QuoteCategory.CONSISTENCY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "cons_04",
            text = "Habits are quiet promises you keep to your future self.",
            author = "to-dodo",
            category = QuoteCategory.CONSISTENCY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "cons_05",
            text = "Even on slow days, doing 70% protects your momentum.",
            author = "to-dodo reminder",
            category = QuoteCategory.CONSISTENCY,
            suitableTime = TimeContext.AFTERNOON
        ),
        MotivationalQuote(
            id = "cons_06",
            text = "The rhythm of daily practice turns ordinary effort into mastery.",
            author = "to-dodo",
            category = QuoteCategory.CONSISTENCY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "cons_07",
            text = "Keep showing up. The results don't always appear immediately.",
            author = "to-dodo reminder",
            category = QuoteCategory.CONSISTENCY,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "cons_08",
            text = "A routine is not a cage; it is a ladder you build for your goals.",
            author = "to-dodo",
            category = QuoteCategory.CONSISTENCY,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "cons_09",
            text = "Another day of showing up. That is how real confidence is forged.",
            author = "to-dodo",
            category = QuoteCategory.CONSISTENCY,
            suitableTime = TimeContext.EVENING
        ),
        MotivationalQuote(
            id = "cons_10",
            text = "Water hollows out stone not by force, but through frequency.",
            author = "to-dodo",
            category = QuoteCategory.CONSISTENCY,
            suitableTime = TimeContext.ANY
        ),

        // ================= DISCIPLINE (9 quotes) =================
        MotivationalQuote(
            id = "disc_01",
            text = "Discipline is simply choosing what you want most over what you want right now.",
            author = "to-dodo reminder",
            category = QuoteCategory.DISCIPLINE,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "disc_02",
            text = "Action creates motivation far more reliably than waiting for a mood.",
            author = "to-dodo",
            category = QuoteCategory.DISCIPLINE,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "disc_03",
            text = "Do the first two minutes. The rest becomes easier once you start.",
            author = "to-dodo reminder",
            category = QuoteCategory.DISCIPLINE,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "disc_04",
            text = "Resisting distraction is a muscle. Train it gently throughout your day.",
            author = "to-dodo",
            category = QuoteCategory.DISCIPLINE,
            suitableTime = TimeContext.AFTERNOON
        ),
        MotivationalQuote(
            id = "disc_05",
            text = "Self-discipline is the highest form of self-respect you can cultivate.",
            author = "to-dodo reminder",
            category = QuoteCategory.DISCIPLINE,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "disc_06",
            text = "Finish what is in front of you before inviting new distractions.",
            author = "to-dodo",
            category = QuoteCategory.DISCIPLINE,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "disc_07",
            text = "Honor your timetable. A well-ordered day brings peace of mind.",
            author = "to-dodo",
            category = QuoteCategory.DISCIPLINE,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "disc_08",
            text = "The hardest part is opening the book or opening the file. Begin.",
            author = "to-dodo reminder",
            category = QuoteCategory.DISCIPLINE,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "disc_09",
            text = "True freedom comes from taking control of your daily choices.",
            author = "to-dodo",
            category = QuoteCategory.DISCIPLINE,
            suitableTime = TimeContext.EVENING
        ),

        // ================= PERSONAL GROWTH (8 quotes) =================
        MotivationalQuote(
            id = "growth_01",
            text = "Better than yesterday. That is the only comparison that matters.",
            author = "to-dodo",
            category = QuoteCategory.PERSONAL_GROWTH,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "growth_02",
            text = "Give yourself permission to be a beginner. Every master started there.",
            author = "to-dodo reminder",
            category = QuoteCategory.PERSONAL_GROWTH,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "growth_03",
            text = "Patience with your process turns daily effort into lasting skill.",
            author = "to-dodo",
            category = QuoteCategory.PERSONAL_GROWTH,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "growth_04",
            text = "Your future self is being shaped by the ordinary tasks of today.",
            author = "to-dodo",
            category = QuoteCategory.PERSONAL_GROWTH,
            suitableTime = TimeContext.AFTERNOON
        ),
        MotivationalQuote(
            id = "growth_05",
            text = "Every mistake is a gentle signpost showing you how to grow.",
            author = "to-dodo reminder",
            category = QuoteCategory.PERSONAL_GROWTH,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "growth_06",
            text = "Celebrate small improvements. They compound into profound shifts.",
            author = "to-dodo",
            category = QuoteCategory.PERSONAL_GROWTH,
            suitableTime = TimeContext.EVENING
        ),
        MotivationalQuote(
            id = "growth_07",
            text = "You don't have to have everything figured out. Just handle today.",
            author = "to-dodo reminder",
            category = QuoteCategory.PERSONAL_GROWTH,
            suitableTime = TimeContext.NIGHT
        ),
        MotivationalQuote(
            id = "growth_08",
            text = "Quiet growth happens away from the spotlight in daily dedication.",
            author = "to-dodo",
            category = QuoteCategory.PERSONAL_GROWTH,
            suitableTime = TimeContext.ANY
        ),

        // ================= EXERCISE (6 quotes) =================
        MotivationalQuote(
            id = "ex_01",
            text = "Movement clears the mind and charges your energy for the day.",
            author = "to-dodo",
            category = QuoteCategory.EXERCISE,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "ex_02",
            text = "A 20-minute workout is always better than the workout you skipped.",
            author = "to-dodo reminder",
            category = QuoteCategory.EXERCISE,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "ex_03",
            text = "Take care of your body. It is the vessel for all your dreams.",
            author = "to-dodo",
            category = QuoteCategory.EXERCISE,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "ex_04",
            text = "Stretch, breathe, and reset. Physical rhythm sharpens mental focus.",
            author = "to-dodo reminder",
            category = QuoteCategory.EXERCISE,
            suitableTime = TimeContext.AFTERNOON
        ),
        MotivationalQuote(
            id = "ex_05",
            text = "Strength isn't built in a day. It is forged in quiet consistency.",
            author = "to-dodo",
            category = QuoteCategory.EXERCISE,
            suitableTime = TimeContext.EVENING
        ),
        MotivationalQuote(
            id = "ex_06",
            text = "Lace up your shoes and step forward. Momentum will meet you there.",
            author = "to-dodo",
            category = QuoteCategory.EXERCISE,
            suitableTime = TimeContext.ANY
        ),

        // ================= READING (5 quotes) =================
        MotivationalQuote(
            id = "read_01",
            text = "A few quiet pages each day opens doors to lifetimes of wisdom.",
            author = "to-dodo",
            category = QuoteCategory.READING,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "read_02",
            text = "Reading is to the mind what regular exercise is to the body.",
            author = "to-dodo reminder",
            category = QuoteCategory.READING,
            suitableTime = TimeContext.AFTERNOON
        ),
        MotivationalQuote(
            id = "read_03",
            text = "Slow down and savor the sentences. Deep thinking needs stillness.",
            author = "to-dodo",
            category = QuoteCategory.READING,
            suitableTime = TimeContext.EVENING
        ),
        MotivationalQuote(
            id = "read_04",
            text = "Ten pages a night is a dozen books a year. Let patience carry you.",
            author = "to-dodo reminder",
            category = QuoteCategory.READING,
            suitableTime = TimeContext.NIGHT
        ),
        MotivationalQuote(
            id = "read_05",
            text = "Feed your thoughts with noble ideas and thoughtful perspectives.",
            author = "to-dodo",
            category = QuoteCategory.READING,
            suitableTime = TimeContext.ANY
        ),

        // ================= CELEBRATION (6 quotes) =================
        MotivationalQuote(
            id = "celeb_01",
            text = "You showed up for yourself all day today. That is worth celebrating! ⭐",
            author = "to-dodo",
            category = QuoteCategory.CELEBRATION,
            suitableTime = TimeContext.EVENING
        ),
        MotivationalQuote(
            id = "celeb_02",
            text = "Planned. Started. Finished. You kept your promise to yourself today ♡",
            author = "to-dodo",
            category = QuoteCategory.CELEBRATION,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "celeb_03",
            text = "100% completed! Take a moment to smile and enjoy this quiet victory 🐣",
            author = "to-dodo",
            category = QuoteCategory.CELEBRATION,
            suitableTime = TimeContext.NIGHT
        ),
        MotivationalQuote(
            id = "celeb_04",
            text = "Full day unlocked! Your dedication today lays the ground for tomorrow ⭐",
            author = "to-dodo reminder",
            category = QuoteCategory.CELEBRATION,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "celeb_05",
            text = "Every box checked. Rest easy tonight knowing you gave today your best ♡",
            author = "to-dodo",
            category = QuoteCategory.CELEBRATION,
            suitableTime = TimeContext.NIGHT
        ),
        MotivationalQuote(
            id = "celeb_06",
            text = "Brilliant execution today! Consistency looks wonderful on you ✨",
            author = "to-dodo reminder",
            category = QuoteCategory.CELEBRATION,
            suitableTime = TimeContext.EVENING
        ),

        // ================= REST (6 quotes) =================
        MotivationalQuote(
            id = "rest_01",
            text = "Rest is not a reward for work; it is an essential part of the rhythm.",
            author = "to-dodo",
            category = QuoteCategory.REST,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "rest_02",
            text = "Not every day needs to be intensely productive to be meaningful 🌿",
            author = "to-dodo reminder",
            category = QuoteCategory.REST,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "rest_03",
            text = "A quiet free day helps you recharge and return with clearer focus.",
            author = "to-dodo",
            category = QuoteCategory.REST,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "rest_04",
            text = "Take a deep breath. Let your mind wander peacefully today ♡",
            author = "to-dodo",
            category = QuoteCategory.REST,
            suitableTime = TimeContext.AFTERNOON
        ),
        MotivationalQuote(
            id = "rest_05",
            text = "Put aside the pressure to do more. Today is for gentle restoration.",
            author = "to-dodo reminder",
            category = QuoteCategory.REST,
            suitableTime = TimeContext.EVENING
        ),
        MotivationalQuote(
            id = "rest_06",
            text = "A restful night's sleep is the foundation of tomorrow's clear thoughts.",
            author = "to-dodo",
            category = QuoteCategory.REST,
            suitableTime = TimeContext.NIGHT
        ),

        // ================= GENERAL (8 quotes) =================
        MotivationalQuote(
            id = "gen_01",
            text = "Small progress is still progress. Keep showing up ♡",
            author = "to-dodo",
            category = QuoteCategory.GENERAL,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "gen_02",
            text = "One task at a time. The mountain will take care of itself.",
            author = "to-dodo reminder",
            category = QuoteCategory.GENERAL,
            suitableTime = TimeContext.MORNING
        ),
        MotivationalQuote(
            id = "gen_03",
            text = "Done is often more valuable than waiting for unattainable perfection.",
            author = "to-dodo",
            category = QuoteCategory.GENERAL,
            suitableTime = TimeContext.AFTERNOON
        ),
        MotivationalQuote(
            id = "gen_04",
            text = "Focus on the effort within your control. Release the rest.",
            author = "to-dodo reminder",
            category = QuoteCategory.GENERAL,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "gen_05",
            text = "Trust your routine. Little daily habits silently build great results.",
            author = "to-dodo",
            category = QuoteCategory.GENERAL,
            suitableTime = TimeContext.ANY
        ),
        MotivationalQuote(
            id = "gen_06",
            text = "Be kind to yourself today. Honest effort is always enough.",
            author = "to-dodo reminder",
            category = QuoteCategory.GENERAL,
            suitableTime = TimeContext.EVENING
        ),
        MotivationalQuote(
            id = "gen_07",
            text = "You don't need a perfect day. You just need another honest step tomorrow.",
            author = "to-dodo",
            category = QuoteCategory.GENERAL,
            suitableTime = TimeContext.NIGHT
        ),
        MotivationalQuote(
            id = "gen_08",
            text = "Make space for what matters. Peace of mind begins with clarity.",
            author = "to-dodo",
            category = QuoteCategory.GENERAL,
            suitableTime = TimeContext.MORNING
        )
    )

    fun getById(id: String): MotivationalQuote? = quotes.find { it.id == id }
}
