package com.sakuraryoko.ap_scan.util;

import de.maxhenkel.audioplayer.api.data.AudioFileOwner;
import de.maxhenkel.audioplayer.audioloader.Metadata;

public class MetadataUtils
{
	public static String toString(Metadata meta)
	{
		StringBuilder sb = new StringBuilder("Metadata[");

		if (meta != null && meta.getAudioId() != null)
		{
			sb.append("{id=").append(meta.getAudioId().toString()).append("}");

			if (meta.getFileName() != null)
			{
				sb.append(",{fileName=").append(meta.getFileName()).append("}");
			}

			if (meta.getVolume() != null)
			{
				sb.append(",{volume=").append(meta.getVolume().toString()).append("}");
			}

			if (meta.getCreated() != null)
			{
				sb.append(",{created=").append(meta.getCreated().toString()).append("}");
			}

			if (meta.getSha256() != null)
			{
				sb.append(",{sha256=").append(meta.getSha256()).append("}");
			}

			if (meta.getOwner() != null)
			{
				AudioFileOwner owner = meta.getOwner();
				sb.append(",{owner=[").append("{uuid=").append(owner.getUUID().toString()).append("},{name=").append(owner.getName()).append("}]}");
			}
		}

		sb.append("]");

		return sb.toString();
	}
}
