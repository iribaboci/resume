package models

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

case class Resume(id: Int,
                  name: String,
                  surname: String,
                  education: Option[Education],
                  experience: Option[Experience],
                  language: Option[Language],
                  certification: Option[Certification],
                  project: Option[Project],
                  hobby: List[String],
                  computerSkills: List[String]) {
  require(name != null, "name not filled")
  require(name != null, "surname not filled")

}
case class Resumes(resumes: Seq[Resume])
case class Education(degree: String, university: String, date: String)
case class Experience(company: String,
                      position: String,
                      location: String,
                      description: String,
                      date: String)
case class Language(language: String, level: String)
case class Certification(name: String, institution: String, date: String)
case class Project(projectName: String, description: String)

object ResumeRegistry {
  // actor protocol
  sealed trait Command
  final case class GetResumes(replyTo: ActorRef[Resumes]) extends Command
  final case class CreateResume(user: Resume,
                                replyTo: ActorRef[ActionPerformed])
      extends Command
  final case class GetResume(name: String, replyTo: ActorRef[GetUserResponse])
      extends Command
  final case class DeleteResume(name: String,
                                replyTo: ActorRef[ActionPerformed])
      extends Command

  final case class GetUserResponse(maybeUser: Option[Resume])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(resumes: Set[Resume]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetResumes(replyTo) =>
        replyTo ! Resumes(resumes.toSeq)
        Behaviors.same
      case CreateResume(resume, replyTo) =>
        replyTo ! ActionPerformed(s"Resume for ${resume.name} created.")
        registry(resumes + resume)
      case GetResume(name, replyTo) =>
        replyTo ! GetUserResponse(resumes.find(_.name == name))
        Behaviors.same
      case DeleteResume(name, replyTo) =>
        replyTo ! ActionPerformed(s"Resume for $name deleted.")
        registry(resumes.filterNot(_.name == name))
    }
}

//object Project {
//  implicit val encoder: Encoder[Project] = (p: Project) => {
//    Json.obj(
//      "projectName" -> p.projectName.asJson,
//      "description" -> p.description.asJson,
//    )
//  }
//  implicit val decoder: Decoder[Project] = (c: HCursor) => {
//    for {
//      projectName <- c.downField("projectName").as[String]
//      description <- c.downField("description").as[String]
//    } yield Project(projectName, description)
//  }
//}
