package net.anotheria.rproxy.refactor.cache.autoexpiry;

/**
 * MetaData class contains data for disk autoexpiry caching which prevents data drop after program stop.
 */
public class MetaData {

    private String fileName;
    private Long creationTimestampSeconds;

    /**
     *
     * @param fileName
     * @param creationTimestampSeconds
     */
    public MetaData(String fileName, Long creationTimestampSeconds) {
        this.fileName = fileName;
        this.creationTimestampSeconds = creationTimestampSeconds;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getCreationTimestampSeconds() {
        return creationTimestampSeconds;
    }

    public void setCreationTimestampSeconds(Long creationTimestampSeconds) {
        this.creationTimestampSeconds = creationTimestampSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetaData metaData = (MetaData) o;

        if (fileName != null ? !fileName.equals(metaData.fileName) : metaData.fileName != null) return false;
        return creationTimestampSeconds != null ? creationTimestampSeconds.equals(metaData.creationTimestampSeconds) : metaData.creationTimestampSeconds == null;
    }

    @Override
    public int hashCode() {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (creationTimestampSeconds != null ? creationTimestampSeconds.hashCode() : 0);
        return result;
    }
}
