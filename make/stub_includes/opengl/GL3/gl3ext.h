#ifndef __gl3ext_h_
#define __gl3ext_h_

#ifndef GL_ARB_geometry_shader4
#define GL_LINES_ADJACENCY_ARB            0x000A
#define GL_LINE_STRIP_ADJACENCY_ARB       0x000B
#define GL_TRIANGLES_ADJACENCY_ARB        0x000C
#define GL_TRIANGLE_STRIP_ADJACENCY_ARB   0x000D
#define GL_PROGRAM_POINT_SIZE_ARB         0x8642
#define GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS_ARB 0x8C29
#define GL_FRAMEBUFFER_ATTACHMENT_LAYERED_ARB 0x8DA7
#define GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS_ARB 0x8DA8
#define GL_FRAMEBUFFER_INCOMPLETE_LAYER_COUNT_ARB 0x8DA9
#define GL_GEOMETRY_SHADER_ARB            0x8DD9
#define GL_GEOMETRY_VERTICES_OUT_ARB      0x8DDA
#define GL_GEOMETRY_INPUT_TYPE_ARB        0x8DDB
#define GL_GEOMETRY_OUTPUT_TYPE_ARB       0x8DDC
#define GL_MAX_GEOMETRY_VARYING_COMPONENTS_ARB 0x8DDD
#define GL_MAX_VERTEX_VARYING_COMPONENTS_ARB 0x8DDE
#define GL_MAX_GEOMETRY_UNIFORM_COMPONENTS_ARB 0x8DDF
#define GL_MAX_GEOMETRY_OUTPUT_VERTICES_ARB 0x8DE0
#define GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS_ARB 0x8DE1
/* reuse GL_MAX_VARYING_COMPONENTS */
/* reuse GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER */
#endif

#ifndef GL_ARB_geometry_shader4
#define GL_ARB_geometry_shader4 1
#ifdef GL_GL3EXT_PROTOTYPES
GLAPI void APIENTRY glProgramParameteriARB (GLuint, GLenum, GLint);
GLAPI void APIENTRY glFramebufferTextureARB (GLenum, GLenum, GLuint, GLint);
GLAPI void APIENTRY glFramebufferTextureLayerARB (GLenum, GLenum, GLuint, GLint, GLint);
GLAPI void APIENTRY glFramebufferTextureFaceARB (GLenum, GLenum, GLuint, GLint, GLenum);
#endif /* GL_GL3EXT_PROTOTYPES */
typedef void (APIENTRYP PFNGLPROGRAMPARAMETERIARBPROC) (GLuint program, GLenum pname, GLint value);
typedef void (APIENTRYP PFNGLFRAMEBUFFERTEXTUREARBPROC) (GLenum target, GLenum attachment, GLuint texture, GLint level);
typedef void (APIENTRYP PFNGLFRAMEBUFFERTEXTURELAYERARBPROC) (GLenum target, GLenum attachment, GLuint texture, GLint level, GLint layer);
typedef void (APIENTRYP PFNGLFRAMEBUFFERTEXTUREFACEARBPROC) (GLenum target, GLenum attachment, GLuint texture, GLint level, GLenum face);
#endif

#endif


