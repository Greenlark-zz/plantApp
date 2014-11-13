package dash.pojo;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.DefaultValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import dash.dao.UserEntity;
import dash.errorhandling.AppException;
import dash.service.PlantService;
import dash.service.GroupService;
import dash.service.PostService;
import dash.service.UserService;

@Component
@Path("/comments")
public class PlantResource {
	@Autowired
	private PlantService plantService;
	
	@Autowired
	private PostService postService;
	
	@Autowired
	private PlantService commentService;
	
	@Autowired
	private GroupService groupService;
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML })
	public Response createPlants(Plant comment) throws AppException {
		Post post= postService.getPostById(comment.getId());
		Group group = groupService.getGroupById(post.getGroup_id());
		
		Long createPlantId = commentService.createPlant(comment, group);
		return Response.status(Response.Status.CREATED)
				// 201
				.entity("A new comment has been created")
				.header("Location",
						"http://localhost:8080/comments/"
								+ String.valueOf(createPlantId)).build();
	}
	
	@POST
	@Path("list")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response createPlants(List<Plant> comments) throws AppException {
		commentService.createPlants(comments);
		return Response.status(Response.Status.CREATED) // 201
				.entity("List of comments was successfully created").build();
	}
	
	
	/**
	 *@param numberOfPlants
	 *-optional
	 *-default is 25
	 *-the size of the List to be returned
	 *
	 *@param startIndex
	 *-optional
	 *-default is 0
	 *-the id of the post you would like to start reading from
	 *
	 *@param group_id
	 *-optional
	 *-if set will attempt to get the requested number of posts from a group.
	 * 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<Plant> getPlants(
			@QueryParam("numberOfPlants") @DefaultValue("25") int numberOfPlants,
			@QueryParam("startIndex") @DefaultValue("0") Long startIndex,
			@QueryParam("post_id") Long post_id)
			throws IOException,	AppException
	{
		if(post_id!=null){
			Post post = postService.getPostById(post_id);
			List<Plant> comments = commentService.getPlantsByPost(numberOfPlants, startIndex, post);
			return comments;
		} else {
			return new ArrayList<Plant>();
		}
	}

	@GET
	@Path("{commonName}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getPlantByCommonName(
			@PathParam("commonName") String commonName,
			@QueryParam("detailed") boolean detailed) throws AppException
	{
		Plant plantByCommonName = plantService.getPlantByCommonName(commonName);
		return Response
				.status(200)
				.entity(new GenericEntity<Plant>(plantByCommonName) {
				},
				detailed ? new Annotation[] { PlantDetailedView.Factory
						.get() } : new Annotation[0])
						.header("Access-Control-Allow-Headers", "X-extra-header")
						.allow("OPTIONS").build();
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<Plant> getPlantsAlpha(
			@QueryParam("native") Integer nativeField,
			@QueryParam("bloom") Integer colorTiming,
			@QueryParam("soil") Integer soilConditions,
			@QueryParam("sun") Integer sunAmount,
			@QueryParam("growthSize") Integer growthSize)
				throws IOException, AppException
	{
		List<Plant> plants = plantService.getPlantsAlpha(nativeField,
				colorTiming, soilConditions, sunAmount, growthSize);
		return plants;
	}
	
	@GET
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getPlantById(@PathParam("id") String id,
			@QueryParam("detailed") boolean detailed)
					throws IOException,	AppException {
		Plant commentById = commentService.getPlantByCommonName(id);
		return Response
				.status(200)
				.entity(new GenericEntity<Plant>(commentById) {
				},
				detailed ? new Annotation[] { PostDetailedView.Factory
						.get() } : new Annotation[0])
						.header("Access-Control-Allow-Headers", "X-extra-header")
						.allow("OPTIONS").build();
	}
	
	/************************ Update Methods *********************/
	
	
	//Full update in not already existing
	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML })
	public Response putPlantById(@PathParam("id") String id, Plant comment)
			throws AppException {
		Plant commentById = commentService.getPlantByCommonName(id);
		
		if (commentById == null) {
			return Response
					.status(Response.Status.NOT_FOUND)
					// 404
					.entity("Plant does not exist with specified id: " + id).build();
		} else {
			// resource is existent and a full update should occur
			commentService.updateFullyPlant(commentById);
			return Response
					.status(Response.Status.OK)
					// 200
					.entity("The comment you specified has been fully updated AT THE LOCATION you specified")
					.header("Location",
							"http://localhost:8888/services/comments/"
									+ String.valueOf(id)).build();
		}
	}

	// PARTIAL update
	@POST
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML })
	public Response partialUpdatePost(@PathParam("id") Long id, Plant comment)
			throws AppException
	{
		comment.setId(id);
		Post post = new Post();
		if(comment.getId() == null) {
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity("Must have set post_id")
					.header("Location",
							"http://localhost:8080/services/posts/"
									+ String.valueOf(post)).build();
		} else {
			post.setId(comment.getId());
		}
		commentService.updateFullyPlant(comment);
		return Response
				.status(Response.Status.OK)
				// 200
				.entity("The comment you specified has been successfully updated")
				.build();
	}

	/*
	 * *********************************** DELETE ***********************************
	 */
	@DELETE
	@Path("{id}")
	@Produces({ MediaType.TEXT_HTML })
	public Response deletePlant(@PathParam("id") String id)
			throws AppException {
		Plant comment = commentService.getPlantByCommonName(id);
		commentService.deletePlant(comment);
		return Response.status(Response.Status.NO_CONTENT)// 204
				.entity("Post successfully removed from database").build();
	}
	
}
