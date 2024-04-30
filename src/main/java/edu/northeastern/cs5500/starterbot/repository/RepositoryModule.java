package edu.northeastern.cs5500.starterbot.repository;

import dagger.Module;
import dagger.Provides;
import edu.northeastern.cs5500.starterbot.model.BehaviorQuestions;
import edu.northeastern.cs5500.starterbot.model.Interviewer;
import edu.northeastern.cs5500.starterbot.model.Interviews;
import edu.northeastern.cs5500.starterbot.model.TechQuestions;
import edu.northeastern.cs5500.starterbot.model.UserPreference;
import edu.northeastern.cs5500.starterbot.model.Users;

@Module
public class RepositoryModule {
    // NOTE: You can use the following lines if you'd like to store objects in memory.
    // NOTE: The presence of commented-out code in your project *will* result in a lowered grade.
    @Provides
    public GenericRepository<UserPreference> provideUserPreferencesRepository(
            InMemoryRepository<UserPreference> repository) {
        return repository;
    }

    // @Provides
    // public GenericRepository<UserPreference> provideUserPreferencesRepository(
    //         MongoDBRepository<UserPreference> repository) {
    //     return repository;
    // }

    // @Provides
    // public Class<UserPreference> provideUserPreference() {
    //     return UserPreference.class;
    // }

    @Provides
    public Class<TechQuestions> provideTechQuestion() {
        return TechQuestions.class;
    }

    @Provides
    public GenericRepository<TechQuestions> provideTechQuestionsRepository(
            MongoDBRepository<TechQuestions> repository) {
        return repository;
    }

    @Provides
    public Class<BehaviorQuestions> provideBehaviorQuestions() {
        return BehaviorQuestions.class;
    }

    @Provides
    public GenericRepository<BehaviorQuestions> provideBehaviorQuestionsRepository(
            MongoDBRepository<BehaviorQuestions> repository) {
        return repository;
    }

    @Provides
    public Class<Interviewer> provideInterviewer() {
        return Interviewer.class;
    }

    @Provides
    public GenericRepository<Interviewer> provideInterviewerRepository(
            MongoDBRepository<Interviewer> repository) {
        return repository;
    }

    @Provides
    public Class<Interviews> provideInterviews() {
        return Interviews.class;
    }

    @Provides
    public GenericRepository<Interviews> provideInterviewsRepository(
            MongoDBRepository<Interviews> repository) {
        return repository;
    }

    @Provides
    public Class<Users> provideUsers() {
        return Users.class;
    }

    @Provides
    public GenericRepository<Users> provideUsersRepository(MongoDBRepository<Users> repository) {
        return repository;
    }
}
