package mobi.chouette.model.api;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Link implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	public static final String GET_METHOD = "get";
	public static final String POST_METHOD = "post";
	public static final String PUT_METHOD = "put";
	public static final String DELETE_METHOD = "delete";

	public static final String LOCATION_REL = "location";
	public static final String CANCEL_REL = "cancel";
	public static final String DELETE_REL = "delete";
	//public static final String DOWNLOAD_REL = "download";
	
	public static final String PARAMETERS_REL = "parameters";
	public static final String ACTION_PARAMETERS_REL = "action_params";
	public static final String VALIDATION_PARAMETERS_REL = "validation_params";
	public static final String DATA_REL = "data";
	public static final String VALIDATION_REL = "validation_report";
	public static final String REPORT_REL = "action_report";

	@Column(name = "type")
	@Getter
	@Setter
	private String type;
 
	@Column(name = "rel")
	@Getter
	@Setter
	private String rel;

	@Column(name = "method")
	@Getter
	@Setter
	private String method;

	@Column(name = "href")
	@Getter
	@Setter
	private String href;



}
