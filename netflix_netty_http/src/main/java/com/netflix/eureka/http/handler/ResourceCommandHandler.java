package com.netflix.eureka.http.handler;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletMappingDescription;
import org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletMappingDetails;
import org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletsMappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.servlet.RequestMappingConditionsDescription;
import org.springframework.boot.actuate.web.mappings.servlet.RequestMappingConditionsDescription.MediaTypeExpressionDescription;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.netflix.eureka.command.CommandResponse;

@Endpoint(id = "getResource")
public class ResourceCommandHandler {
	
	private final String path;
	
	private final ApplicationContext application;
	private final DispatcherServletsMappingDescriptionProvider description;
	
	public ResourceCommandHandler(String path,
			DispatcherServletsMappingDescriptionProvider descriptionProviders, 
			ApplicationContext applicationContext) {
		this.path = path;
		this.application = applicationContext;
		this.description = descriptionProviders;
	}
	
	@ReadOperation
    public CommandResponse<Map<String, Uris>> handle() {
		ApplicationContext target = this.application;
		Map<String, Uris> paths = new HashMap<>();
		while (mappingsForContext(target, paths) != null) {
			target = target.getParent();
		}
		return CommandResponse.ofSuccess(paths);
    }
	
	private Map<String, Uris> mappingsForContext(ApplicationContext applicationContext, Map<String, Uris> paths) {
		if(applicationContext == null) {
			return null;
		}
		Map<String, List<DispatcherServletMappingDescription>> mappings = description.describeMappings(applicationContext);
		if(mappings != null) {
			mappings.forEach((name, provider) -> describe(provider.stream(), paths));
		}
		return paths;
	}
	
	private Map<String, Uris> describe(Stream<DispatcherServletMappingDescription> stream, Map<String, Uris> paths) {
		stream.forEachOrdered(dispatcherServlet -> {
			DispatcherServletMappingDetails details = dispatcherServlet.getDetails();
			if(details != null) {
				RequestMappingConditionsDescription requestMappingConditions = details.getRequestMappingConditions();
				if(requestMappingConditions != null ) {
					requestMappingConditions.getPatterns().forEach(api -> {
						if(!(api.startsWith(path) || api.equals("/error"))) {
							paths.put(api, mapOperations(requestMappingConditions, ofNullable(paths.get(api))));
						}
					});
				}
			}
		});
		return paths;
	}
	
	private Uris mapOperations(RequestMappingConditionsDescription request, Optional<Uris> existingPath) {
		Uris path = existingPath.orElse(new Uris());
		nullToEmptyList(request.getMethods()).forEach(method -> {
			String name = method.name().toLowerCase();
			path.set(name, path.now(name, request.getProduces()
					.stream().map(MediaTypeExpressionDescription::getMediaType)
					.collect(Collectors.toList())));
		});
		return path;
	}
	
	private Set<RequestMethod> nullToEmptyList(Set<RequestMethod> method) {
		if(method == null) {
			return new HashSet<RequestMethod>();
		}
		return method;
	}
	
	@JsonPropertyOrder({"get", "head", "post", "put", "delete", "options", "patch"})
	public static final class Uris {
		private Uri get;
		private Uri put;
		private Uri head;
		private Uri post;
		private Uri delete;
		private Uri patch;
		private Uri options;

		public Uris set(String method, Uri uri) {
			if ("get".equals(method)) {
	            return get(uri);
	        }
	        if ("put".equals(method)) {
	            return put(uri);
	        }
	        if ("head".equals(method)) {
	            return head(uri);
	        }
	        if ("post".equals(method)) {
	            return post(uri);
	        }
	        if ("delete".equals(method)) {
	            return delete(uri);
	        }
	        if ("patch".equals(method)) {
	            return patch(uri);
	        }
	        if ("options".equals(method)) {
	            return options(uri);
	        }
	        return null;
		}
		
		@JsonIgnore
	    public List<Uri> all() {
	        List<Uri> all = new ArrayList<>();
	        if (get != null) {
	        	all.add(get);
	        }
	        if (put != null) {
	        	all.add(put);
	        }
	        if (head != null) {
	        	all.add(head);
	        }
	        if (post != null) {
	        	all.add(post);
	        }
	        if (delete != null) {
	        	all.add(delete);
	        }
	        if (patch != null) {
	        	all.add(patch);
	        }
	        if (options != null) {
	        	all.add(options);
	        }

	        return all;
	    }

		public Uris get(Uri get) {
	        this.get = get;
	        return this;
	    }

	    public Uris head(Uri head) {
	        this.head = head;
	        return this;
	    }

	    public Uris put(Uri put) {
	        this.put = put;
	        return this;
	    }

	    public Uris post(Uri post) {
	        this.post = post;
	        return this;
	    }

	    public Uris delete(Uri delete) {
	        this.delete = delete;
	        return this;
	    }

	    public Uris patch(Uri patch) {
	        this.patch = patch;
	        return this;
	    }

	    public Uris options(Uri options) {
	        this.options = options;
	        return this;
	    }
	    
	    public Uri now(String method, List<String> produces) {
	    	return new Uri(method, produces);
	    }
	    
		public class Uri {
			private String method;
			private List<String> produces;
			
			public Uri() {}
			
			public Uri(String method, List<String> produces) {
				this.method = method;
				this.produces = produces;
			}

			public String getMethod() {
				return method;
			}

			public List<String> getProduces() {
				return produces;
			}
		}

		public Uri getGet() {
			return get;
		}

		public Uri getPut() {
			return put;
		}

		public Uri getHead() {
			return head;
		}

		public Uri getPost() {
			return post;
		}

		public Uri getDelete() {
			return delete;
		}

		public Uri getPatch() {
			return patch;
		}

		public Uri getOptions() {
			return options;
		}
	}
	
}
